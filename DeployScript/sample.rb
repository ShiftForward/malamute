require_relative 'deploy_lib'

if __FILE__ == $0
  p1 = Project.open_project('malamute')
  p2 = Project.open_project('adstax')
  #p1 = Project.new_project('malamute','Deploy Logger Service','https://bitbucket.org/shiftforward/malamute')
  #p2 = Project.new_project('adstax','adstax','https://bitbucket.org/shiftforward/adstax')
  sleep 5
  puts Project.get_projects
  puts p1.add_deploy('Last deploy version', 'https://bitbucket.org/shiftforward/malamute', 'v0.1', false, 'none')
  puts p2.add_deploy('Last deploy version', 'https://bitbucket.org/shiftforward/adstax', 'v0.1', false, 'xpto')
  sleep 2
  n = rand(0..3)
  if n == 0
    puts p1.add_deploy_event(DeployStatus::FAILED, "Done.")
    puts p2.add_deploy_event(DeployStatus::SUCCESS, "Done.")
  elsif n == 1
    puts p1.add_deploy_event(DeployStatus::SUCCESS, "Done.")
    puts p2.add_deploy_event(DeployStatus::FAILED, "Done.")
  elsif n == 2
    puts p1.add_deploy_event(DeployStatus::LOG, "Information.")
    puts p2.add_deploy_event(DeployStatus::LOG, "Information.")
  else
    puts p1.add_deploy_event(DeployStatus::SKIPPED, "Done.")
    puts p2.add_deploy_event(DeployStatus::SKIPPED, "Done.")
  end
end