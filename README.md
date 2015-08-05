## Malamute

### Deploy Logger Service

The Deploy Logger Service is built for documenting and record the release of new versions of projects (deploys) and it's modules.

The core of the project was developed using Scala and [Spray](http://spray.io/) providing a RESTful API documented trough [Swagger](http://swagger.io/). 
A webapp for viewing information built in [Backbone](http://backbonejs.org/) and [Bootstrap Paper Template](https://bootswatch.com/paper/).

Additionally, there is a Ruby SDK for easy integration in Ruby based systems.

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

In addition to the main project there is a Ruby SDK that simplifies the process of integration the Deploy Logger Service. 
An example of using is available at `/DeployScript/sample.rb`

## Authors

_Malamute_ was created by João Pedro Dias and supported by Luís Fonseca e Bruno Maia at [ShiftForward](http://www.shiftforward.eu/).

## Contributing

We encourage you to contribute to _Malamute_! Submit bug reports and suggestions for improvements through GitHub's issues and your own improvements through pull requests. 

## Licenses

_malamute_ is licensed under MIT. See LICENSE for details.

[![ShiftForward](http://cdn.shiftforward.eu/wp-content/uploads/2015/01/ShiftForward_logo_new-01.png)](http://www.shiftforward.eu/)