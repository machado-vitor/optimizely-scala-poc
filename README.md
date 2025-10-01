1 - How Optimizely works?

Datafile - Project’s flags, experiments, rules, audiences, variables and traffic allocation live in a datafile JSON.
This JSON is a representation of the current sate of flag rules for an environment.
Sync methods:
- Pull method (recommended on doc)
- Push method (webhook)
- Customized method (CDN datafile URL)
  https://docs.developers.optimizely.com/feature-experimentation/docs/example-datafile

Evaluation - The SDK deterministically buckets the UserContext (userId + attributes) per flag/experiment and returns a Decision (flag on/off, variation, variables, reasons).

```OptimizelyUserContext user = optimizely.createUserContext("user123");
// Option 1 – Create a user, then set attributes.
user.setAttribute("is_logged_in", false);
user.setAttribute("app_version", "1.3.2");
```
// Option 2 – Pass attributes when creating the user.
```Map<String, Object> attributes = new HashMap<>();
attributes.put("is_logged_in", false);
attributes.put("app_version", "1.3.2");
OptimizelyUserContext user = optimizely.createUserContext("user123", attributes);
```

Return of Decision can be made via method user.decide(flagKey) or user.decideAll

https://docs.developers.optimizely.com/feature-experimentation/docs/create-user-context-java

Events/stats - The SDK batches decision and conversion events to Optimizely for Stats Engine analysis.

https://docs.developers.optimizely.com/feature-experimentation/docs/event-batching-java


2 - Main concepts

* Flag: A feature toggle that can have deliveries (rollouts) and/or experiments (A/B).
  * https://docs.developers.optimizely.com/feature-experimentation/docs/create-feature-flags
* Experiment/Rule/Variation: Traffic is allocated by a rule; users land in a variation. Variations can carry variables (config).
  * https://docs.developers.optimizely.com/feature-experimentation/docs/create-flag-variables
* Audiences / Attributes: Targeting criteria from user attributes.
* User Context: Input (id + attributes) for decisions and tracking.
  * https://docs.developers.optimizely.com/feature-experimentation/docs/create-user-context-java
* Datafile: JSON config snapshot the SDK uses to evaluate decisions.
  * https://docs.developers.optimizely.com/feature-experimentation/docs/example-datafile
* Decide / DecideAll: APIs to get one or all flag decisions for a user.
  * https://docs.developers.optimizely.com/feature-experimentation/docs/optimizelyusercontext-java
