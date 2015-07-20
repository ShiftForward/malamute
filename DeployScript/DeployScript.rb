require 'net/http'
require 'uri'
require 'json'

URL = 'http://localhost:8000/'

module DeployStatus
  STARTED = "STARTED"
  SKIPPED = "SKIPPED"
  FAILED = "FAILED"
  SUCCESS = "SUCCESS"
end

class Project

  def initialize()
  end

  def post_project(name, description, git)
    uri = URI.parse(URL+"project")

    req = Net::HTTP::Post.new(uri.request_uri)
    req['Content-Type'] = 'application/json'

    req.body = {
        name: name,
        description: description,
        git: git
    }.to_json

    res = Net::HTTP.start(uri.hostname, uri.port) do |http|
      http.request(req)
    end
    
    puts res.body
    
    if res.kind_of?(Net::HTTPSuccess)
      return JSON.parse(res.body)['name']
    end
  end

  def get_projects
    uri = URI.parse(URL+"projects")

    req = Net::HTTP::Get.new(uri.request_uri)

    res = Net::HTTP.start(uri.hostname, uri.port) do |http|
      http.request(req)
    end
    puts res.body
  end

  def get_project(name)
    uri = URI.parse(URL + "project/#{name}")

    req = Net::HTTP::Get.new(uri.request_uri)
    req['Content-Type'] = 'application/json'

    res = Net::HTTP.start(uri.hostname, uri.port) do |http|
      http.request(req)
    end
    puts res.body
  end

  def add_deploy(proj_name, description, changelog, version, automatic, client)
    #git configurations for current folder
    user =  `git config --get user.name`.chomp!
    commit_branch = `git rev-parse --abbrev-ref HEAD`.chomp!
    commit_hash = `git rev-parse HEAD`.chomp!
    
    uri = URI.parse(URL+"project/#{proj_name}/deploy")

    req = Net::HTTP::Post.new(uri.request_uri)
    req['Content-Type'] = 'application/json'

    req.body = {
        user: user,
        commit: {
        branch: commit_branch,
        hash: commit_hash
    },
        description: description,
        changelog: changelog,
        version: version,
        automatic: automatic,
        client: client
    }.to_json

    res = Net::HTTP.start(uri.hostname, uri.port) do |http|
      http.request(req)
    end
    puts res.body
    return JSON.parse(res.body)['id']
  end

  
  def add_deploy_event(projname, deployid, status, description)
    uri = URI.parse(URL + "project/#{projname}/deploy/#{deployid}/event")

    req = Net::HTTP::Post.new(uri.request_uri)
    req['Content-Type'] = 'application/json'

    req.body = {
        status: status,
        description: description
    }.to_json

    res = Net::HTTP.start(uri.hostname, uri.port) do |http|
      http.request(req)
    end
    puts res.body
  end
end


if __FILE__ == $0
  proj_name = "Malamute_FINAL"
  p = Project.new()
  p.post_project(proj_name,'Deploy Logger Service','https://bitbucket.org/shiftforward/malamute')
  p.get_projects
  p.get_project(proj_name)
  lastdeployid = p.add_deploy(proj_name, 'Intial deploy version', 'https://bitbucket.org/shiftforward/malamute', 'v0.1', false, 'none')
  puts lastdeployid
  p.add_deploy_event(proj_name, lastdeployid, DeployStatus::SKIPPED, "Done.")
  p.add_deploy_event(proj_name, lastdeployid, DeployStatus::SUCCESS, "Done.")
end