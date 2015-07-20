require 'net/http'
require 'uri'
require 'json'

URL = 'http://localhost:8000/'

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
    
    uri = URI.parse(URL+"project" + "/" + proj_name + "/" + "deploy")

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
  p = Project.new()
  p.post_project('Malamute','Deploy Logger Service','https://bitbucket.org/shiftforward/malamute')
  p.get_projects
  p.get_project('Malamute')
  lastdeployid = p.add_deploy('Malamute', 'Intial deploy version', 'https://bitbucket.org/shiftforward/malamute', 'v0.1', false, 'none')
  puts lastdeployid
  p.add_deploy_event('Malamute', lastdeployid, "SUCCESS", "Done.")
end