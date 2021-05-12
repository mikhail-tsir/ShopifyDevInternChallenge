![screencap](https://github.com/mikhail-tsir/ShopifyDevInternChallenge/blob/main/public/images/screenshot.png)
# Image Repository App
An Image repository app built with the [Play framework](https://www.playframework.com/) in Scala.

You can play around with it here: [https://intense-coast-99475.herokuapp.com/](https://intense-coast-99475.herokuapp.com/)
(Try searching for the username `mikhail`, you'll find an example page!)

I wanted to challenge myself and learn something new, so I decided to do
take on functional programming and do the whole thing in Scala.

It ended up being a great learning experience and I think Scala is an
underrated language (for backend development at least, I head it's pretty
popular in the data world). Here's why:
* Strong, static type system
* Functional programming: You get immutability, minimal side effects,
  functions as first-class objects, etc.
  
* Object-oriented: You still get classes and interfaces and such
* More expressive than Java: Much less boilerplate code
* No nullPointerExceptions

### Tools used
* Postgres for database
* AWS s3 for cloud storage
* [Slick](https://scala-slick.org/) for DB queries
* [Play framework](https://www.playframework.com/) for the app itself

## Features
* User authentication (sign up/log in)
* Creating photo albums (public and private)
* Uploading images to albums
* Deleting images from an album
* Deleting entire albums
* A somewhat appealing UI

## To run locally
I was using an Ubuntu 18.04 machine, so I hope the mac instructions work as well.
### Prerequisites
#### Java:
Install the JDK

Linux:
```bash
sudo apt install default-jdk
```

Mac
```bash
brew tap AdoptOpenJDK/openjdk
brew cask install adoptopenjdk8
```
#### Postgres
Linux:
Follow the steps [here](https://www.postgresql.org/download/linux/ubuntu/)

Mac:
```bash
brew install postgresql
```

To create a local database run the following locally in postgres:
```postgresql
CREATE DATABASE test;
```
and save your postgres credentials
```bash
export POSTGRES_USER=your_username
export POSTGRES_PASSWORD=your_password
```

#### sbt
sbt is the build tool used to build scala projects

Linux:
```bash
echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list
echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | sudo tee /etc/apt/sources.list.d/sbt_old.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo apt-key add
sudo apt-get update
sudo apt-get install sbt
```
Mac:
```bash
brew install sbt
```

#### AWS
Make sure you have AWS credentials, and save the credentials in environment variables:
```bash
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_ACCESS_KEY_ID=your_access_key_id
export AWS_DEFAULT_REGION=your_region
```
Create an s3 bucket and save the bucket name
```bash
export IMAGE_REPO_BUCKET_NAME=your_bucket_name
````
### Clone into the repo:
```bash
git clone git@github.com:mikhail-tsir/ShopifyDevInternChallenge.git
```

And finally,
### Run the app
```bash
sbt run
```
and navigate to `http://localhost:9000`. You may be prompted with `Database needs evolution`, in which case you can click
`Apply evolution`.

### Run tests
```
sbt test
```
# Helpful resources
* [jwt.io](https://jwt.io/introduction) For understanding JWT authentication
* [This guy's videos](https://www.youtube.com/channel/UCRS4DvO9X7qaqVYUW2_dwOw). He
explains Functional Programming and Scala concepts very well, even though it's 
  not related to Play specifically.

* This example project for using Slick with Play: [https://github.com/playframework/play-samples/tree/2.8.x/play-scala-slick-example](https://github.com/playframework/play-samples/tree/2.8.x/play-scala-slick-example)
