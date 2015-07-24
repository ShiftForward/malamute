require_relative 'deploy_lib'  

if __FILE__ == $0
  Project.new_project('malamute','Deploy Logger Service','https://bitbucket.org/shiftforward/malamute')
  sleep 5
  puts Project.open_project('malamute')
  puts Project.get_projects
  puts Project.add_deploy('Intial deploy version', 'https://bitbucket.org/shiftforward/malamute', 'v0.1', false, 'none')
  sleep 2
  n = rand(0..2)
  if n == 0
    puts Project.add_deploy_event(DeployStatus::FAILED, "Done.")
  elsif n == 1
    puts Project.add_deploy_event(DeployStatus::SUCCESS, "Done.")
  else
    puts Project.add_deploy_event(DeployStatus::SKIPPED, "Done.")
  end
end