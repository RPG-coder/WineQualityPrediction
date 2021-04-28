package rp39;

/*
 * Name: Rahul Gautham Putcha
 * NJIT-ID: 31524074
 * -----------------------
 * Program: WineQualityTesting
 * Details: Parallel Training on SPARK-HADOOP (EMR)
 */
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.ml.feature.VectorAssembler;

//import org.apache.spark.ml.classification.DecisionTreeClassifier;
//import org.apache.spark.ml.classification.DecisionTreeClassificationModel;

//import org.apache.spark.ml.classification.RandomForestClassificationModel;
//import org.apache.spark.ml.classification.RandomForestClassifier;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;

import org.apache.spark.api.java.*;

/**
 * Class: WineQualityTesting
 * Details: Wine Quality Testing; Testing wine quality prediction on scale using 
 * previously saved model of RandomForestClassifier. 
 * NOTE: The Model and datasets are picked from s3 and put back into s3
 * NOTE: S3 Must have read and write access.
 */
public class WineQualityTesting
{
    public static void main( String[] args ) throws Exception
    {
      Logger.getLogger("org.apache").setLevel(Level.WARN);
      String model_file = "s3n://pr2-mllib/sample-model";
      if(args.length<1 || args.length>2){
	  System.out.println(
		"ERROR: Input the S3 bucket with the testfile as 1 single arg.\n"+
		"Example ==> \n"+
		"arg[0] = s3n://pr2-mllib/TestDataset.csv, and\n"+
		"(Optional) ... arg[1] = s3n://pr2-mllib/sample-model\n"
	  );
          return;
      }
      if(args.length==2){
        model_file = args[1];
	System.out.println("INFO: Accessing model "+model_file);
      }
      SparkSession spark = SparkSession.builder()
	      .appName("Wine Quality Prediction")
	      .master("local[*]") // To-be run on a single EC2 instance
	      .getOrCreate();

      Dataset<Row> test_dataset = spark.read()
	      .option("header",true)
	      .option("inferSchema", "true")
	      .option("delimiter", ";")
	      .csv(args[0]);

      // dataset.show(5);
      //train_dataset.printSchema();

      String[] col_names = new String[]{
	      "fixed acidity","volatile acidity", "citric acid", "residual sugar",
	      "chlorides", "free sulfur dioxide", "total sulfur dioxide", "density",
	      "pH", "sulphates", "alcohol","quality"
      };
      int i=0;
      for (String column : test_dataset.columns()) {
	if(i<col_names.length){
          test_dataset = test_dataset.withColumnRenamed(column, col_names[i]);
	}
	i++;
      }
      
      // Transformations
      VectorAssembler vectorAssembler = new VectorAssembler();
      vectorAssembler.setInputCols(col_names).setOutputCol("features");
      Dataset<Row> testDataset = vectorAssembler.transform(test_dataset).select("features","quality");

      //inputDataset.show(5);
      
      //Training
      //DecisionTreeClassifier model = new DecisionTreeClassifier()
      //RandomForestClassifier model = new RandomForestClassifier().setFeaturesCol("features");
      try{
      	PipelineModel p_model = PipelineModel.load(model_file);

      	Dataset<Row> predictions = p_model.transform(testDataset);
    
      	// Testing and Evaluation 
      	MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
	.setLabelCol("quality")
  	.setPredictionCol("prediction")
  	.setMetricName("accuracy");
      	double accuracy = evaluator.evaluate(predictions);
      	System.out.println("Accuracy: "+accuracy);
      }catch(Exception e){
	System.out.println(
	  model_file+" doesnot exist!\n"+
	  "Please execute Training program\n"
	);
      }

    }
}
