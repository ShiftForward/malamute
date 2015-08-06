## Deploy Logger Ruby SDK

## Example

```ruby

  URL = "http://localhost:8000/api/"

  sdk = DeployLoggerSDK.new(URL)
  
  p1 = sdk.open_project('malamute')
  
  d1 = p1.new_deploy('Added bootstrap.', 'http://pastebin.com/HtdMtgiz',
                'v0.1.1', false, 'Nexus','This is config.')
  .with_module("LogBack","SNAPSHOT 2.0",ModuleStatus::ADD)
  .with_module("SBT","SNAPSHOT 2.9.0",ModuleStatus::ADD)
  .with_module("Spray","SNAPSHOT 2.5",ModuleStatus::ADD)
  .start()
  .add_event(DeployStatus::LOG, "Script started.")

  d1.add_event(DeployStatus::FAILED, "Failed due to timeout.")
  
```

## Public access methods

- `DeployLoggerSDK.new(URL)`: Instantiates a new SDK.
- `sdk.open_roject(proj_name)`: Opens a project.
- `sdk.new_project(proj_name, description, git_url)`: Creates a new project.
- `project.new_deploy(descriptio, changelog_url, version, is_automatic, client, config`: Creates a new deploy.
- `deploy.with_module(module_name, version, status)`: Add or remove a module.
- `deploy.start()`: Store the deploy data.
- `deploy.add_event(event_type, description)`: Added an event to deploy.

### Types of deploy event.

```ruby

  DeployStatus::STARTED
  DeployStatus::SKIPPED
  DeployStatus::FAILED
  DeployStatus::LOG
  DeployStatus::SUCCESS
  
```

### Types of module status.

```ruby

  ModuleStatus::ADD
  ModuleStatus::REMOVE
  
```