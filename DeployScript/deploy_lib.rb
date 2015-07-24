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
  
  @project_name = ""
  @lastdeployid = ""
  
  def self.new_project(name, description, git)
    uri = URI.parse(URL + "project")

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
    
    if res.kind_of?(Net::HTTPSuccess)
      @project_name = name
    end
      res.body
  end

  def self.get_projects
    uri = URI.parse(URL + "projects")

    req = Net::HTTP::Get.new(uri.request_uri)

    res = Net::HTTP.start(uri.hostname, uri.port) do |http|
      http.request(req)
    end
    res.body
  end

  def self.open_project(name)
    uri = URI.parse(URL + "project/#{name}")

    req = Net::HTTP::Get.new(uri.request_uri)
    req['Content-Type'] = 'application/json'

    res = Net::HTTP.start(uri.hostname, uri.port) do |http|
      http.request(req)
    end
    if res.kind_of?(Net::HTTPSuccess)
      @project_name = name
    end
    res.body
  end

  def self.add_deploy(description, changelog, version, automatic, client)
    #git configurations for current folder
    user =  `git config --get user.name`.chomp!
    commit_branch = `git rev-parse --abbrev-ref HEAD`.chomp!
    commit_hash = `git rev-parse HEAD`.chomp!
    
    uri = URI.parse(URL + "project/#{@project_name}/deploy")

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
    if res.kind_of?(Net::HTTPSuccess)
      @lastdeployid = JSON.parse(res.body)['id']
    end
    res.body
  end

  
  def self.add_deploy_event(status, description)
    uri = URI.parse(URL + "project/#{@project_name}/deploy/#{@lastdeployid}/event")

    req = Net::HTTP::Post.new(uri.request_uri)
    req['Content-Type'] = 'application/json'

    req.body = {
        status: status,
        description: description
    }.to_json

    res = Net::HTTP.start(uri.hostname, uri.port) do |http|
      http.request(req)
    end
    res.body
  end
end