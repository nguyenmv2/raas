---
layout: default
title:  "Development tips"
---

Generally during development you'll need two processes:

* sbt running the backend server
* grunt server which automatically picks up any changes

## Cloning

If you are planning to use Raas as scaffolding for your own project, consider cloning the repo with `git clone --depth 1` in order to start the history with last commit. You can now switch to your origin repository of choice with: `git remote set-url origin https://repo.com/OTHERREPOSITORY.git`

## Useful sbt commands

* `renameProject` - replace Raas with your custom name and adjust scala package names
* `compile` - compile the whole project
* `test` - run all the tests
* `project <sub-project-name>` - switch context to the given sub-project, then all the commands will be executed only for
that sub-project, this can be also achieved with e.g.: `<sub-project-name>/test`
* `~backend/re-start` - runs the backend server and waits for source code changes to automatically compile changed file and to reload it

## Database schema evolution

With Flyway, all you need to do is to put DDL script within raas-backend/src/main/resources/db/migration/ directory. You have to obey the following [naming convention](http://flywaydb.org/documentation/migration/sql.html): `V#__your_arbitrary_description.sql` where `#` stands for *unique* version of your schema.

## Developing frontend without backend

If you'd like to work only on the frontend, without starting the backend, you can proxy requests to a working, remote backend instance. In `ui/Gruntfile.js` you need to edit the proxy settings and change the `proxies` option in `connect` to point to the backend instance, e.g.:

```
proxies: [ {
    context: '/rest/',
    host: 'my-backend.com',
    port: 80,
    headers: {
        'host': 'my-backend.com'
    }
} ]
```

## Imports

There are three imports that are useful when developing a new functionality:

### JSON

If you are doing JSON serialisation or deserialisation, or if you are defining an endpoint which uses JSON bodies, add the following import:

```scala
import com.github.nguyenmv2.raas.infrastructure.Json._
```

This will bring into scope both custom and built-in [Circe](https://github.com/circe/circe) encoders/decoders.

### Database

If you are defining database queries or running transactions, add the following import:

```scala
import com.github.nguyenmv2.raas.infrastructure.Doobie._
```

This will bring into scope both custom and built-in [doobie](https://tpolecat.github.io/doobie/) metas.

### HTTP API

Finally, if you are describing new endpoints, import all members of the current `Http` instance:

```scala
import com.github.nguyenmv2.raas.http.Http

class UserApi(http: Http) {
  import http._

  ...
}
```

This will bring into scope tapir builder methods and custom schemas for documentation.
Note that if you are using JSON in your endpoint descriptions, you'll need the JSON imports as well.
