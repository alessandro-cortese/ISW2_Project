package view;

import enums.*;
import model.ClassifierEvaluation;
import model.JavaClass;
import model.ReleaseInfo;
import org.jetbrains.annotations.NotNull;
import utils.AcumeUtils;
import utils.FileUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.nio.file.Path;

public class FileCreator {

    private FileCreator() {}

    public static void writeCsvForAcume(String projectName, ClassifierEnum classifierEnum, FeatureSelectionEnum featureSelectionEnum, SamplingEnum samplingEnum, CostSensitiveEnum costSensitiveEnum, Integer index, List<AcumeUtils> acumeUtils) throws IOException {

        String fileName = projectName.toUpperCase() + "_" + classifierEnum.toString().toUpperCase() + "_" + featureSelectionEnum.toString().toUpperCase() + "_" + samplingEnum.toString().toUpperCase() + "_" + costSensitiveEnum.toString().toUpperCase() + "_" + index.toString();
        File file = createANewFileAcume(projectName, fileName, FilenamesEnum.ACUME, index);

        writeOnCsvAcume(file, acumeUtils);

    }

    private static @NotNull File createANewFileAcume(String projectName, String fileName, FilenamesEnum fileEnum, int fileIndex) throws IOException {
        String enumFilename = FileUtils.enumToFilename(fileEnum, fileIndex);
        Path dirPath = Path.of("retrieved_data/" + projectName + "/acume/");
        return getFile(fileName, ".csv", enumFilename, dirPath, true);
    }

    private static @NotNull File getFile(String projName, String endPath, String enumFilename, Path dirPath, boolean acume) throws IOException {

        Path pathname;

        if(!acume) {
            pathname = Path.of(dirPath.toString(), projName + enumFilename + endPath);
        }else{
            projName = projName + ".csv";
            pathname = Path.of(dirPath.toString(), projName);
        }

        return getFile(dirPath, pathname);
    }

    private static void writeOnCsvAcume(File file, List<AcumeUtils> acumeUtilsList) throws IOException {

        try(FileWriter fileWriter = new FileWriter(file)) {

            fileWriter.write("ID," + "Size," + "Predicted," + "Actual");

            fileWriter.write("\n");

            for(AcumeUtils acumeUtils : acumeUtilsList) {

                fileWriter.write(acumeUtils.getIndex() + ",");                          //INDEX OF CLASS
                fileWriter.write(acumeUtils.getSize() + ",");                           //SIZE OF CLASS
                fileWriter.write(acumeUtils.getProbabilityOfBuggyness() + ",");         //PROBABILITY OF BUGGY
                fileWriter.write(acumeUtils.isBuggy());

                fileWriter.write("\n");

            }

        }

    }

    private static @NotNull File createANewFile(String projName, FilenamesEnum fileEnum, int fileIndex, String endPath) throws IOException {
        String enumFilename = FileUtils.enumToFilename(fileEnum, fileIndex);
        Path dirPath = Path.of("retrieved_data/", projName, FileUtils.enumToDirectoryName(fileEnum));

        Path pathname = Path.of(dirPath.toString(), projName + enumFilename + endPath);

        return getFile(dirPath, pathname);
    }

    private static @NotNull File getFile(Path dirPath, Path pathname) throws IOException {
        File dir = new File(dirPath.toUri());
        File file = new File(pathname.toUri());

        if(!dir.exists() && !file.mkdirs()) {
            throw new RuntimeException(); //Exception: dir creation impossible
        }

        if(file.exists() && !file.delete()) {
            throw new IOException(); //Exception: file deletion impossible
        }

        return file;
    }

    public static void writeOnCsv(String projName, List<ReleaseInfo> rcList, FilenamesEnum csvEnum, int csvIndex) throws IOException {

        File file = createANewFile(projName, csvEnum, csvIndex, ".csv");

        try(FileWriter fw = new FileWriter(file)) {

            fw.write("VERSION," +
                    "JAVA_CLASS," +
                    "SIZE," +
                    "LOC_ADDED," +
                    "MAX_LOC_ADDED," +
                    "AVG_LOC_ADDED," +
                    "LOC_DELETED," +
                    "MAX_LOC_DELETED," +
                    "AVG_LOC_DELETED," +
                    "CHURN," +
                    "MAX_CHURN," +
                    "AVG_CHURN," +
                    "FIXED_DEFECTS," +
                    "NUMBER_OF_COMMITS," +
                    "NUMBER_OF_AUTHORS," +
                    "IS_BUGGY\n");

            writeClassesDataOnFile(rcList, fw, false);
        }
    }

    private static void writeClassesDataOnFile(List<ReleaseInfo> riList, FileWriter fw, boolean isArff) throws IOException {
        int count;
        for(ReleaseInfo releaseInfo: riList) {
            count = 0;
            for(JavaClass javaClass: releaseInfo.getJavaClasses()) {

                if(!isArff) {
                    fw.write(releaseInfo.getRelease().getIndex() + ","); //VERSION
                    fw.write(javaClass.getName() + ","); //JAVA_CLASS
                }
                fw.write(javaClass.getMetrics().getSize() + ",");                           //SIZE
                fw.write(javaClass.getMetrics().getLocAdded() + ",");                       //LOC_ADDED
                fw.write(javaClass.getMetrics().getMaxLocAdded() + ",");                    //MAX_LOC_ADDED
                fw.write(javaClass.getMetrics().getAvgLocAdded() + ",");                    //AVG_LOC_ADDED
                fw.write(javaClass.getMetrics().getLocDeleted() + ",");                     //LOC_DELETED
                fw.write(javaClass.getMetrics().getMaxLocDeleted() + ",");                  //MAX_LOC_DELETED
                fw.write(javaClass.getMetrics().getAvgLocDeleted() + ",");                  //AVG_LOC_DELETED
                fw.write(javaClass.getMetrics().getChurn() + ",");                          //CHURN
                fw.write(javaClass.getMetrics().getMaxChurn() + ",");                       //MAX_CHURN
                fw.write(javaClass.getMetrics().getAvgChurn() + ",");                       //AVG_CHURN
                fw.write(javaClass.getMetrics().getFixedDefects() + ",");                   //FIXED_DEFECTS
                fw.write(javaClass.getCommits().size() + ",");                              //NUMBER_OF_COMMITS
                fw.write(javaClass.getMetrics().getnAuth() + ",");                          //NUMBER_OF_AUTHORS
                fw.write(javaClass.getMetrics().isBuggy());                                     //IS_BUGGY

                fw.write("\n");

                if(javaClass.getMetrics().isBuggyness()) {
                    count++;
                }
            }

            releaseInfo.setBuggyClasses(count);
        }
    }

    public static void writeEvaluationDataOnCsv(String projName, List<ClassifierEvaluation> classifierEvaluationList) throws IOException {

        File file = createANewFile(projName, FilenamesEnum.EVALUATING, 0, ".csv");

        try(FileWriter fw = new FileWriter(file)) {

            fw.write("DATASET," +
                    "#TRAINING_RELEASES," +
                    "%TRAINING_INSTANCES," +
                    "CLASSIFIER," +
                    "FEATURE_SELECTION," +
                    "BALANCING," +
                    "COST_SENSITIVE," +
                    "PRECISION," +
                    "RECALL," +
                    "AUC," +
                    "KAPPA," +
                    "TRUE_POSITIVE," +
                    "FALSE_POSITIVE," +
                    "TRUE_NEGATIVE," +
                    "FALSE_NEGATIVE\n");

            for(ClassifierEvaluation classifierEvaluation: classifierEvaluationList) {

                fw.write(projName + ",");                                                   //DATASET
                fw.write(classifierEvaluation.getWalkForwardIterationIndex() + ",");        //#TRAINING_RELEASES
                fw.write(classifierEvaluation.getTrainingPercent() + ",");                  //%TRAINING_INSTANCES
                fw.write(classifierEvaluation.getClassifier() + ",");                       //CLASSIFIER
                fw.write(classifierEvaluation.getFeatureSelection().toString() + ",");      //FEATURE_SELECTION
                fw.write(classifierEvaluation.getSampling().toString() + ",");              //BALANCING
                fw.write(classifierEvaluation.getCostSensitiveType().toString() + ",");     //COST_SENSITIVE
                fw.write(classifierEvaluation.getPrecision() + ",");                        //PRECISION
                fw.write(classifierEvaluation.getRecall() + ",");                           //RECALL
                fw.write(classifierEvaluation.getAuc() + ",");                              //AUC
                fw.write(classifierEvaluation.getKappa() + ",");                            //KAPPA
                fw.write(classifierEvaluation.getTp() + ",");                               //TRUE_POSITIVE
                fw.write(classifierEvaluation.getFp() + ",");                               //FALSE_POSITIVE
                fw.write(classifierEvaluation.getTn() + ",");                               //TRUE_NEGATIVE
                fw.write(classifierEvaluation.getFn() + "\n");                              //FALSE_NEGATIVE
            }
        }
    }

    public static void writeOnArff(String projName, List<ReleaseInfo> riList, FilenamesEnum filenamesEnum, int fileIndex) throws IOException {

        File file = createANewFile(projName, filenamesEnum, fileIndex, ".arff");

        try(FileWriter fw = new FileWriter(file)) {

            fw.write("@relation " + file.getName() + "\n");
            fw.write("@attribute SIZE numeric\n");
            fw.write("@attribute LOC_ADDED numeric\n");
            fw.write("@attribute MAX_LOC_ADDED numeric\n");
            fw.write("@attribute AVG_LOC_ADDED numeric\n");
            fw.write("@attribute LOC_DELETED numeric\n");
            fw.write("@attribute MAX_LOC_DELETED numeric\n");
            fw.write("@attribute AVG_LOC_DELETED numeric\n");
            fw.write("@attribute CHURN numeric\n");
            fw.write("@attribute MAX_CHURN numeric\n");
            fw.write("@attribute AVG_CHURN numeric\n");
            fw.write("@attribute FIXED_DEFECTS numeric\n");
            fw.write("@attribute NUMBER_OF_COMMITS numeric\n");
            fw.write("@attribute NUMBER_OF_AUTHORS numeric\n");
            fw.write("@attribute IS_BUGGY {'True', 'False'}\n");
            fw.write("@data\n");

            writeClassesDataOnFile(riList, fw, true);

        }
    }
}