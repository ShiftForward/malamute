#!/usr/bin/env ruby

require_relative 'deploy_lib'

if __FILE__ == $0

  URL = "http://localhost:8000/api/"

  sdk = DeployLoggerSDK.new(URL)
  p1 = sdk.open_project('malamute')
  #p1 = sdk.new_project('malamute',
  #                     'Deploy Logger Service',
  #                     'https://github.com/ShiftForward/malamute')
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
    .with_module("LogBack","SNAPSHOT 8.1",ModuleStatus::ADD)
    .with_module("SBT","SNAPSHOT 8.8",ModuleStatus::ADD)
    .with_module("Spray","SNAPSHOT 8.9",ModuleStatus::ADD)
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
  .with_module("Backbone","SNAPSHOT 9.1",ModuleStatus::ADD)
  .with_module("Akka","SNAPSHOT 9.3",ModuleStatus::ADD)
  .with_module("Angular","v9.2.3",ModuleStatus::ADD)
  .start().add_event(DeployStatus::LOG, "Script started.")

  n = rand(0..2)
  if n == 0
    d2.add_event(DeployStatus::SKIPPED, "Already deployed.")
    d1.add_event(DeployStatus::FAILED, "Failed due to timeout.")
  elsif n == 1
    d2.add_event(DeployStatus::FAILED, "Failed due to timeout.")
    d1.add_event(DeployStatus::SUCCESS, "Success.")
  else
    d2.add_event(DeployStatus::SUCCESS, "All good.")
    d1.add_event(DeployStatus::SKIPPED, "Scheduled for next week.")
  end

end