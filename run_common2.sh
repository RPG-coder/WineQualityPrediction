cd $SPARK_HOME/conf/
sudo cp spark-env.sh.template spark-env.sh
echo SPARK_MASTER_HOST=$1 | sudo tee -a spark-env.sh
cd $SPARK_HOME/sbin
sudo sh ./start-master.sh
sudo java -cp $SPARK_HOME/conf/:$SPARK_HOME/jars/* -Xmx1g org.apache.spark.deploy.master.Master --host $1 --port 7077 --webui-port 80

cd ~
wget https://mirrors.ocf.berkeley.edu/apache/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz
tar xvf apache-maven-3.6.3-bin.tar.gz
sudo mv apache-maven-3.6.3  /usr/local/apache-maven
export M2_HOME=/usr/local/apache-maven
export M2=$M2_HOME/bin 
export PATH=$M2:$PATH
mvn -version

