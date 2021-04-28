package rp39;

/*
 * Name: Rahul Gautham Putcha
 * NJIT-ID: 31524074
 * -----------------------
 * Program: WineQualityTraining
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

import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.ml.classification.RandomForestClassifier;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;

import org.apache.spark.api.java.*;

/**
 * Class: WineQualityTraining
 * Details: Wine Quality Training; Training for predicting wine quality on scale of
 * 10 using RandomForestClassifier. 
 * The Dataset is picked from s3 and put back into s3
 * NOTE: S3 Must have read and write access.
 */
public class WineQualityTraining
{
    public static void main( String[] args ) throws Exception
    {
      Logger.getLogger("org.apache").setLevel(Level.WARN);
      String model_file = "s3n://pr2-mllib/sample-model";
      if(args.length<2 || args.length>3){
	System.out.println("ERROR: Missing following,\n"+
		"arg[0]=(URI of S3 bucket with training file) and,"+
	        "arg[1]=(URI of S3 bucket with validation file)\n"+
		"(Optional) ... arg[2] = (URI of bucket hosting that will host save model file)\n"+
		"NOTE: Both args must be of s3n://pr2-mllib/dataset.csv form\n"+
		"Syntax:\n"+
		"arg[0]='s3n://pr2-mllib/TrainingDataset.csv', and "+
		"arg[1]='s3n://pr2-mllib/ValidationDataset.csv'\n"+
		"(Optional) ... arg[2] = s3n://pr2-mllib/sample_model , ... (is the default)"+
		", NOTE: Filename also is included!!"

	);
	return;
      }if(args.length==3){
	model_file = args[2];
      }
      SparkSession spark = SparkSession.builder()
	      .appName("Wine Quality Prediction")
	      .getOrCreate();

      Dataset<Row> train_dataset = spark.read()
	      .option("header",true)
	      .option("inferSchema", "true")
	      .option("delimiter", ";")
	      .csv(args[0]);
      Dataset<Row> valid_dataset = spark.read()
              .option("header",true)
              .option("inferSchema", "true")
              .option("delimiter", ";")
              .csv(args[1]);
      // dataset.show(5);
      //train_dataset.printSchema();

      String[] col_names = new String[]{
	      "fixed acidity","volatile acidity", "citric acid", "residual sugar",
	      "chlorides", "free sulfur dioxide", "total sulfur dioxide", "density",
	      "pH", "sulphates", "alcohol", "quality"
      };
      int i=0;
      for (String column : train_dataset.columns()) {
        train_dataset = train_dataset.withColumnRenamed(column, col_names[i]);
	i++;
      }
      i=0;
      for (String column : valid_dataset.columns()) {
        valid_dataset = valid_dataset.withColumnRenamed(column, col_names[i]);
	i++;
      }
      // Transformations (For both Training and Validations
      VectorAssembler vectorAssembler = new VectorAssembler();
      vectorAssembler.setInputCols(new String[] {
	      "fixed acidity","volatile acidity", "citric acid", "residual sugar",
              "chlorides", "free sulfur dioxide", "total sulfur dioxide", "density",
              "pH", "sulphates", "alcohol"
      }).setOutputCol("features");
      Dataset<Row> inputDataset = vectorAssembler.transform(train_dataset)
	      .select("quality","features")
	      .withColumnRenamed("quality", "label");
      Dataset<Row> testDataset = vectorAssembler.transform(valid_dataset)
              .select("quality","features")
              .withColumnRenamed("quality", "label");

      //inputDataset.show(5);
      
      //Training
      //DecisionTreeClassifier model = new DecisionTreeClassifier()
      RandomForestClassifier model = new RandomForestClassifier()
  	.setLabelCol("label")
  	.setFeaturesCol("features");

      Pipeline pipeline = new Pipeline().setStages(new PipelineStage[]{model});
      PipelineModel p_model = pipeline.fit(inputDataset);

      Dataset<Row> predictions = p_model.transform(testDataset);
    
      // Testing and Evaluation 
      MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
  	.setLabelCol("label")
  	.setPredictionCol("prediction")
  	.setMetricName("accuracy");
      double accuracy = evaluator.evaluate(predictions);
      System.out.println("Accuracy: "+accuracy);

      try{
      	p_model.save(model_file);
      }catch(Exception e){
        System.out.println("ERROR: A `sample-model` named model file with same name already exist.");
      }
    }
}
