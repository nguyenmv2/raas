---
layout: default
title:  "Production deployment"
---

## Fat jar

To build an executable jar, simply run (in sbt) `backend/assembly` (that is, the `assembly` task in the `backend` subproject). This will create a fat-jar with all the code, processed javascript, css and html. You can run the jar simply by running java:

```
java -jar backend/target/scala-2.12/raas.jar
```

## Docker

To build a docker image, run `backend/docker:publishLocal`. This will create the `docker:latest` image.

You can test the image by using the provided `docker-compose.yml` file.

## Heroku

Raas-based applications can be easily deployed to [Heroku](https://www.heroku.com).

First, you need to create a Heroku account and install the [toolbelt](https://toolbelt.heroku.com).
Once this is done, login to heroku from the command line with `heroku login` while in the application's main directory.

From there you can create a new application, e.g.:

````
heroku create myappname
````

You now have a new application, which should be also visible in Heroku's web console. For convenience, you can set the
app as the default for the project:

```
heroku git:remote -a myappname
```

Using a file-system based H2 database on a non-dev environment isn't probably a good choice, so you can add a free, entry-level Postgres database with

````
heroku addons:create heroku-postgresql:hobby-dev
````

Raas already includes the Postgres driver and properly recognizes the `DATABASE_URL` environment variable that is set by Postgres Heroku.

Now you can deploy your app. Raas includes an sbt task which will build the fat-jar and upload it:

````
sbt deployHeroku
````

After that's done you can visit your application's URL. If anything goes wrong, `heroku logs` will show you your application's output.
