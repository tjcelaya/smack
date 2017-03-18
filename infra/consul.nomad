#     https://www.nomadproject.io/docs/job-specification/job.html
job "consul" {
  # region = "global"

  #required
  datacenters = ["dc1"]

  #     https://www.nomadproject.io/docs/jobspec/schedulers.html
  type = "service"

  #     https://www.nomadproject.io/docs/job-specification/update.html
  update {
    stagger = "10s"
    max_parallel = 1
  }

  #     https://www.nomadproject.io/docs/job-specification/group.html
  group "group" {
    count = 1
    restart {
      attempts = 10
      interval = "5m"
      delay = "25s"

     # "delay" mode delays the next
     # restart until the next interval. "fail" mode does not restart the task
     # if "attempts" has been hit within the interval.
      mode = "delay"
    }

    #     https://www.nomadproject.io/docs/job-specification/ephemeral_disk.html

    #     https://www.nomadproject.io/docs/job-specification/task.html
    task "consul" {
      driver = "docker"

      config {
        image = "consul:latest"
        volumes = [
          "/tmp:/tmp"
        ]
        port_map {
          # db =
          #8300-8302/tcp,
          #8400/tcp,
          #8301-8302/udp,
          #8600/tcp,
          #8600/udp,
          consul_web = 8500
        }
      }

      resources {
        cpu    = 500 # 500 MHz
        memory = 256 # 256MB
        network {
          mbits = 10
          port "consul_web" {}
        }
      }

      #     https://www.nomadproject.io/docs/job-specification/service.html
      service {
        name = "global-consul-check"
        tags = ["global", "consul"]
        port = "consul_web"
        check {
          name     = "alive"
          type     = "tcp"
          interval = "10s"
          timeout  = "2s"
        }
      }

      # kill_timeout = "20s"
    }
  }
}
