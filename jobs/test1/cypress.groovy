
class JobConfig {

  static final String JENKINS_CREDENTIALS_FOR_GITHUB = "chitsgithub"

  static
  def basicPipeline(job, repo, includeBranches = "master PR-*", ignoreOnPush = false, buildPR = true, jenkinsfilePath = "Jenkinsfile", interval = 0) {

    job.with {
      configure {
        it / factory(class: 'org.jenkinsci.plugins.workflow.multibranch.WorkflowBranchProjectFactory') {
          owner(class: 'org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject', reference: '../..')
          scriptPath(jenkinsfilePath)
        }

        it / sources(class: 'jenkins.branch.MultiBranchProject$BranchSourceList') / 'data' / "jenkins.branch.BranchSource" {
          source(class: "org.jenkinsci.plugins.github_branch_source.GitHubSCMSource") {
            id(UUID.randomUUID())
            credentialsId(JENKINS_CREDENTIALS_FOR_GITHUB)
            repoOwner('chit787')
            repository(repo)
            ignoreOnPushNotifications(true)

            traits {
              'org.jenkinsci.plugins.github__branch__source.BranchDiscoveryTrait' {
                strategyId('1')
              }
              if (buildPR) {
                'org.jenkinsci.plugins.github__branch__source.OriginPullRequestDiscoveryTrait' {
                  strategyId('1')
                }
              }
              'jenkins.scm.impl.trait.WildcardSCMHeadFilterTrait' {
                includes(includeBranches)
                excludes()
              }
              'org.jenkinsci.plugins.github__branch__source.SSHCheckoutTrait' {
                credentialsId(JENKINS_CREDENTIALS_FOR_GITHUB)
              }
              'jenkins.plugins.git.traits.CloneOptionTrait' {
                extension(class: "hudson.plugins.git.extensions.impl.CloneOption") {
                  shallow("false")
                  noTags("false")
                  depth("0")
                  honorRefspec("false")
                }
              }
              if (ignoreOnPush) {
                'jenkins.plugins.git.traits.IgnoreOnPushNotificationTrait'()
              }
            }
          }
          strategy(class: "jenkins.branch.DefaultBranchPropertyStrategy") {
            properties(class: "empty-list")
          }
        }
      }

      orphanedItemStrategy {
        discardOldItems {
          daysToKeep(1)
        }
      }

      triggers {
        if (interval > 0) {
          periodic(interval)
        }
      }

    }
  }
}

folder('Cypress Development')

def cypressPipeline = multibranchPipelineJob('Cypress Development/cypress Pipeline')
JobConfig.basicPipeline(cypressPipeline, repo = "cypress", includeBranches="master PR-*")

