Name: Rahul Gautham Putcha
NJIT-ID: 31524074

## Installation steps

NOTE-1: Training is conducted Manual way, using AWS EMR(Elastic Map Reduce setup).
NOTE-2: Testing can be done either without (Manual way) or with using Docker.
NOTE-3: Project files are hosted at Github, https://github.com/RPG-coder/WineQualityPrediction
NOTE-4: Docker files are hosted at DockerHub, https://hub.docker.com/repository/docker/rahulgputcha/wine-quality-predictor
        or by, $ docker pull rahulgputcha/wine-quality-predictor

Following sections provides for step by step setup for the entire project:

------- Training only (START)---------------
### I. Setup (Setting EMR / Managed-Hadoop cluster on AWS)
To achieve parallelism we require a method to access files in a distributed systems. Hence, to save the burden of setup AWS provides EMR Elastic Map Reduce and We opt to choose the variant Spark on Hadoop YARN. 
Steps:
1. Choose EMR AWS Service
2. Click on Create a Cluster
3. Fill in the details
   i. Enter cluster name
   ii. In Software configuration, Choose Spark: Spark 2.4.7 on Hadoop 2.10.1 YARN and Zeppelin 0.9.0
   iii. In Hardware configuration, Choose m5.xlarge and set number of instances to 4 instances
4. Select a Key-pair and Click on `Create`.


### II. Create S3 bucket 
In order to make the files accessed by all of our workers in Spark, we require a persistent distributed storage providing read access to our EC2.
(Note: we cannot store our file in a single system, as each system is secured and difficult to access normally).
Steps:
1. Choose EMR S3 Service
2. Create Bucket and enter bucket name, say `pr2-mllib`
3. Click on `Create bucket`
4. Within the bucket name `pr2-mllib`, add the dataset files (training and validation dataset)
   i. Click on `Upload Files`
   ii. Click on `Add files` and select files from local system
   iii. Finally, Click on `Upload`
   
### III. Execute the Project files
Before Login setup, we need to edit the security groups to provide SSH access to the instance using our local machine.
Pre-Steps:
1. In the `Cluster` page and under `Security and access`, click on the `Security groups for Master`: `(..random Id...)`
2. In the security group page select select security group belonging to `EMR-master`
3. Click on `Edit inbound rules`
4. Click on `Add rules`
   i. Add SSH type and select source as `My IP`
   This will give ssh access solely to your local machine
5. click `Save rules`

### IV. Install Maven inside EMR (IMPORTANT AND USEFUL FOR Single machine prediction application)
Execute following steps to install maven and setup project files

\# Linux-step by step setup for Apache Maven
$ cd ~
$ wget https://mirrors.ocf.berkeley.edu/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz
$ tar xvf apache-maven-3.6.3-bin.tar.gz
$ sudo mv apache-maven-3.6.3  /usr/local/apache-maven
$ export M2_HOME=/usr/local/apache-maven
$ export M2=$M2_HOME/bin 
$ export PATH=$M2:$PATH
$ mvn -version

### V. Project Setup
$ mvn archetype:generate -DgroupId=rp39 -DartifactId=app-pr2 -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false

Now we have to download WineQualityTraining.java and WineQualityTesting.java to app-pr2/src/main/java/rp39/
And download pom.xml file to app-pr2/, hence replace the old pom.xml file with the new one.
# After the setup, in the directory where the pom.xml resides, execute following commands:
$ mvn package 

### VI. Training Execution
\# If all is good, execute the following
$ spark-submit --class rp39.WineQualityTraining target/app-pr2-1.0-SNAPSHOT.jar s3n://pr2-mllib/TrainingDataset.csv s3n://pr2-mllib/TestingDataset.csv
\# NOTE: Do change the TrainingDataset and TestingDataset in accordance to the `bucket-name`
\# NOTE: by default, a saved sample model is sent to, s3n://pr2-mllib/ , as s3n://pr2-mllib/sample-model
We can change this configuration by using an optional argument,
$ spark-submit --class rp39.WineQualityTraining target/app-pr2-1.0-SNAPSHOT.jar \
s3n://pr2-mllib/TrainingDataset.csv \
s3n://pr2-mllib/TestingDataset.csv \
s3n://bucket-name/output-model-name

This end the Training phase and we are provided with a sample model stored inside the S3 bucket

------- Training only (END)---------------\


------- Testing only (START)---------------\

To carry out the testing
Steps:
1. Start an EC2 Instance of any custom configuration. Specific changes related to Security Groups are as follow:
   HTTP   TCP  80	   <<MyIP>>	
   Custom TCP  8080  <<MyIP>>
   SSH    TCP  22	   <<MyIP>>
   Custom TCP	7077  <<MyIP>>
2. We can perform the setup of this phase in one of two ways given below:
   i. Without Docker:
      $ sh ./run_common1.sh # To install Apache Spark/ Java/ Scala and envs
      $ source ~/.bash_profile
      $ sh ./run_common2.sh # To setup Apache Spark and Maven
      Then follow the section on `IV. Install Maven inside EMR` and `V. Project Setup`, this time we are setting our project for testing on a 
      seperate EC2 instance. After setting up `Maven` and project, we now execute the WineQualityTesting.java
      
      $ spark-submit --class rp39.WineQualityTesting target/app-pr2-1.0-SNAPSHOT.jar s3n://pr2-mllib/TestDataset.csv
      \# NOTE: Do change the TrainingDataset and TestingDataset in accordance to the `bucket-name`
      \# NOTE: by default, the sample model is loaded from, s3n://pr2-mllib/ , as s3n://pr2-mllib/sample-model
      We can change this configuration by using the optional argument as below,
      $ spark-submit --class rp39.WineQualityTesting target/app-pr2-1.0-SNAPSHOT.jar \
      s3n://pr2-mllib/TestDataset.csv \
      s3n://bucket-name/output-model-name
      
   ii. With using Docker:
   1. Get the docker image from docker hub (Auto-run included)
   $ docker push rahulgputcha/wine-quality-predictor
   To execute docker image or to create a docker container
   2. docker run rahulgputcha/wine-quality-predictor TestingFilenameLocation.csv {save_model_path}
      - NOTE: TestingFilenameLocation.csv can be fetched from s3 bucket using location as, s3n://pr-mllib/TestingFilenameLocation.csv
   
   
------------------------------------------------------------------------
                                 Happy using
------------------------------------------------------------------------
