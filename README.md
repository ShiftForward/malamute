## Malamute

### Deploy Logger Service

![DeployLoggerService](http://i.imgur.com/es3RMZy.png)

The Deploy Logger Service is built for documenting and record the release of new versions of projects (deploys) and its modules.

The core of the project was developed using Scala and [Spray](http://spray.io/) providing a RESTful API documented through [Swagger](http://swagger.io/). 
A webapp for viewing information built in [Backbone](http://backbonejs.org/) and [Bootstrap Paper Template](https://bootswatch.com/paper/).

Additionally, there is a [Ruby SDK](SDK-Ruby) for easy integration in Ruby based systems.

## Screenshots

### Projects Dashboard

![Dashboard](http://i.imgur.com/4iLCgOP.png)

### Project Deploys

![Deploys](http://i.imgur.com/Poyi3kr.png)


### Project Modules

![Modules](http://i.imgur.com/b0w8xeC.png)

### Deploy Details

![Deploy Details](http://i.imgur.com/bxrCpOs.png)

## Configuration

A configuration file is available in `/src/main/resources/reference.conf`.

    logger-service {
      apiVerion = "0.1"
      apiConfig = ${api-config}
      persistence = ${slick-db.sqlite}
      interface = "0.0.0.0"
      port = 8000
    }

## API Reference

    get /api/project/{projName}/deploy/{deployId}
        Returns a Deploy
    get /apiproject/{projName}/clients
        Returns a List of Clients
    post /api/project/{projName}/deploy/{deployId}/event
        Add a event to a deploy
    get /api/project/{projName}/client/{clientName}
        Returns a Module List
    delete /api/project/{projName}
        Returns a Project
    get /api/project/{projName}
        Returns a Project
    get /api/project/{projName}/deploys
        Returns a List of Deploy
    post /api/project/{projName}/deploy
        Returns a Deploy
    post /api/project
        Returns a Project
    get /api/projects
        Returns an array of Projects
    get /api/ping
        Returns a pong

More information is available at Swagger on `http://localhost:8000/swagger`

### Using the API

With this API you can easily integrate the _Deploy Logger Service_ with your existent deploy scripts or services like [jenkins-ci](https://jenkins-ci.org/).

Example of a cURL for creating a project:

```
curl -X POST --header "Content-Type: application/json; charset=UTF-8" --header "Accept: */*" -d "{
  \"name\": \"malamute\",
  \"description\": \"Deploy Logger Service\",
  \"git\": \"https://github.com/ShiftForward/malamute\"
}" "http://localhost:8000/api/project"
```

## Build & Run

### Building

    sbt compile
    
### Run
    
    sbt run
    
### Test

Tests are present in `/test/scala/eu.shiftforward.deploylogger/DeployLoggerRouteSpec.scala`

To run the tests simple do
    
    sbt test
    
## Utilities

In addition to the main project there is a [Ruby SDK](SDK-Ruby) that simplifies the process of integration the Deploy Logger Service. 
An example of using is available at `/DeployScript/sample.rb`

## Authors

_Malamute_ was created by [Jo�o Pedro Dias](http://jpdias.github.io) and supported by [Bruno Maia](https://brunomaia.eu/) and [Lu�s Fonseca](http://www.pimentelfonseca.pt/) at [ShiftForward](http://www.shiftforward.eu/).

## Contributing

We encourage you to contribute to _Malamute_! Submit bug reports and suggestions for improvements through GitHub's issues and your own improvements through pull requests. 

## Licenses

_malamute_ is licensed under MIT. See LICENSE for details.

[![ShiftForward](http://cdn.shiftforward.eu/wp-content/uploads/2015/01/ShiftForward_logo_new-01.png)](http://www.shiftforward.eu/)
