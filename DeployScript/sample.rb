#!/usr/bin/env ruby

require_relative 'deploy_lib'

if __FILE__ == $0
  p1 = Project.open_project('malamute')

  d1 = p1.new_deploy('TESTESTE version', 'https://bitbucket.org/shiftforward/malamute',
                     'v0.2132', false, 'KuantoKusta', "This config")

  puts d1.with_module("ABCD!","SNAPSHOT 2.0",ModuleStatus::ADD)

  puts d1.start_deploy()

  puts d1.add_deploy_event(DeployStatus::LOG, "Information.")
  n = rand(0..2)
  if n == 0
    puts d1.add_deploy_event(DeployStatus::FAILED, "Failed.")
  elsif n == 1
    puts d1.add_deploy_event(DeployStatus::SUCCESS, "Success.")
  else
    puts d1.add_deploy_event(DeployStatus::SKIPPED, "Skipped.")
  end
    
  d1.with_client("BlaBlaXPTO")
  puts d1.with_modules([
    ["ADxda","SNAPSHOT 2.0",ModuleStatus::ADD],
    ["xptox","SNAPSHOT 2.1",ModuleStatus::ADD],
    ["xyz","SNAPSHOT 2.1a",ModuleStatus::ADD]
  ])
  d1.start_deploy()

  
  puts d1.add_deploy_event(DeployStatus::LOG, "Information.")
  n = rand(0..2)
  if n == 0
    puts d1.add_deploy_event(DeployStatus::FAILED, "Failed.")
  elsif n == 1
    puts d1.add_deploy_event(DeployStatus::SUCCESS, "Success.")
  else
    puts d1.add_deploy_event(DeployStatus::SKIPPED, "Skipped.")
  end
end