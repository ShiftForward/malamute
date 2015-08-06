#!/usr/bin/env ruby

require_relative 'deploy_lib'

if __FILE__ == $0

  URL = "http://localhost:8000/api/"

  sdk = DeployLoggerSDK.new(URL)
  p1 = sdk.open_project('malamute')
  p2 = sdk.open_project('ridgeback')
  #p2 = sdk.new_project('ridgeback',
  #                     'Continuous integration service for performance tests',
  #                     'https://github.com/ShiftForward/ridgeback')
  d1 = p1.new_deploy('Added bootstrap.', 'http://pastebin.com/HtdMtgiz',
                'v0.1.1', false, 'Nexus', %(logger-service {
                                            apiVerion = "0.1"
                                            apiConfig = ${api-config}
                                            persistence = ${slick-db.sqlite}
                                            interface = "0.0.0.0"
                                            port = 8000
                                          })
  )
    .with_module("LogBack","SNAPSHOT 2.0",ModuleStatus::ADD)
    .with_module("SBT","SNAPSHOT 2.9.0",ModuleStatus::ADD)
    .with_module("Spray","SNAPSHOT 2.5",ModuleStatus::ADD)
    .start().add_event(DeployStatus::LOG, "Script started.")

  d2 = p2.new_deploy('Update Angular version.', 'http://pastebin.com/HtdMtgiz',
                     'v0.1.5', false, 'TypeSafe', %(logger-service {
                                            apiVerion = "0.1"
                                            apiConfig = ${api-config}
                                            persistence = ${slick-db.sqlite}
                                            interface = "0.0.0.0"
                                            port = 8000
                                          })
  )
  .with_module("Backbone","SNAPSHOT 2.1",ModuleStatus::ADD)
  .with_module("Akka","SNAPSHOT 3.1",ModuleStatus::ADD)
  .with_module("Angular","v3.0.0",ModuleStatus::ADD)
  .start()

end