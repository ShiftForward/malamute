require 'net/http'
require 'uri'
require 'json'

module DeployStatus
  STARTED = "STARTED"
  SKIPPED = "SKIPPED"
  FAILED = "FAILED"
  LOG = "LOG"
  SUCCESS = "SUCCESS"
end

module ModuleStatus
  REMOVE = "REMOVE"
  ADD = "ADD"
end

class DeployLoggerSDK

  @deployLoggerServiceURL = ""

  def initialize(url)
    @deployLoggerServiceURL = url
  end

  def new_project(name, description, git)
    uri = URI.parse(@deployLoggerServiceURL + "project")

    req = Net::HTTP::Post.new(uri.request_uri)
    req['Content-Type'] = 'application/json'

    req.body = {
        name : name,
        description : description,
        git : git
    }.to_json

    res = Net::HTTP.start(uri.hostname, uri.port) do |http|
      http.request(req)
    end

    if res.kind_of?(Net::HTTPSuccess)
      return Project.new(name, @deployLoggerServiceURL)
    else
      raise "Code: #{res.code} : #{res.body}"
    end
  end

  def open_project(name)
    uri = URI.parse(@deployLoggerServiceURL + "project/#{name}")

    req = Net::HTTP::Get.new(uri.request_uri)
    req['Content-Type'] = 'application/json'

    res = Net::HTTP.start(uri.hostname, uri.port) do |http|
      http.request(req)
    end
    if res.kind_of?(Net::HTTPSuccess)
      return Project.new(name, @deployLoggerServiceURL)
    else
      raise "Code: #{res.code} : #{res.body}"
    end
  end
end

class Project

  def initialize(project_name, url)
    @deployLoggerServiceURL = url
    @project_name = project_name
  end

  def new_deploy(description, changelog, version, automatic, client, configuration)
    Deploy.new(description, changelog, version, automatic, client, configuration,
               @project_name, @deployLoggerServiceURL)
  end

end

class Deploy

  def initialize(description, changelog, version, automatic, client, configuration, project_name, deployLoggerServiceURL)
    @description = description
    @changelog = changelog
    @version = version
    @modules = Array.new
    @configuration = configuration
    @automatic = automatic
    @client = client
    @last_deploy_id = ""
    @project_name = project_name
    @deployLoggerServiceURL = deployLoggerServiceURL
  end

  def start()
    #git configurations for current folder
    user = `git config --get user.name`.chomp!
    commit_branch = `git rev-parse --abbrev-ref HEAD`.chomp!
    commit_hash = `git rev-parse HEAD`.chomp!

    uri = URI.parse(@deployLoggerServiceURL + "project/#{@project_name}/deploy")

    req = Net::HTTP::Post.new(uri.request_uri)
    req['Content-Type'] = 'application/json'

    req.body = {
        user : user,
        commit : {
        branch : commit_branch,
        hash : commit_hash
    },
        description : @description,
        changelog : @changelog,
        version : @version,
        automatic : @automatic,
        client : @client,
        modules : @modules,
        configuration : @configuration
    }.to_json

    res = Net::HTTP.start(uri.hostname, uri.port) do |http|
      http.request(req)
    end
    if res.kind_of?(Net::HTTPSuccess)
      @last_deploy_id = JSON.parse(res.body)['id']
    else
      raise "Code: #{res.code} : #{res.body}"
    end
    self
  end

  def with_module(name, version, status)
    @modules.push({:name => "#{name}", :version => "#{version}", :status => "#{status}"})
    self
  end

  def add_event(status, description)
    uri = URI.parse(@deployLoggerServiceURL + "project/#{@project_name}/deploy/#{@last_deploy_id}/event")

    req = Net::HTTP::Post.new(uri.request_uri)
    req['Content-Type'] = 'application/json'

    req.body = {
        status : status,
        description : description
    }.to_json

    res = Net::HTTP.start(uri.hostname, uri.port) do |http|
      http.request(req)
    end
    if res.kind_of?(Net::HTTPSuccess)
      res.body
    else
      raise "Code: #{res.code} : #{res.body}"
    end
    self
  end

end