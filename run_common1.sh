sudo yum update -y
sudo yum install wget -y
sudo yum install java-1.8.0-openjdk* -y
sudo ln -s /usr/lib/jvm/VERSION /usr/lib/jvm/default-java
sudo wget http://www.scala-lang.org/files/archive/scala-2.11.1.tgz
sudo tar xvf scala-2.11.1.tgz
sudo mv scala-2.11.1 /usr/lib
sudo ln -s /usr/lib/scala-2.11.1 /usr/lib/scala

sudo yum install ntp ntpdate ntp-doc -y
sudo service ntpd restart

sudo wget http://d3kbcqa49mib13.cloudfront.net/spark-2.0.0-bin-hadoop2.7.tgz
sudo tar xf spark-2.0.0-bin-hadoop2.7.tgz
sudo mkdir /usr/local/spark
sudo cp -r spark-2.0.0-bin-hadoop2.7/* /usr/local/spark
echo 'export PATH=$PATH:/usr/lib/scala/bin' >> ~/.bash_profile
echo 'export PATH=$PATH:/usr/lib/jvm/default-java/bin' >> ~/.bash_profile
echo 'export PATH=$PATH:/usr/local/spark/bin ' >> ~/.bash_profile
echo 'export SPARK_HOME=/usr/local/spark' >> ~/.bash_profile

