val loginChain = chain.exec(http("First Request Chain").get(baseUrl)).pause(1,2)

val loginGroup = "Login"
val doStuffGroup = "Do Stuff"


val lambdaUser = scenario("Standard User")
  .insertChain(loginChain)
  // First request outside iteration
  .exec(http("Catégorie Poney").get(baseUrl).capture(xpath("//input[@id='text1']/@value") in "aaaa_value"))
  .pause(pause2, pause3)
  .doFor(12000, TimeUnit.MILLISECONDS,
      chain
        .exec(http("In During 1").get(baseUrl))
        .pause(2)
        .exec(http("In During 2").get(baseUrl))
        .pause(2))
  // Loop
  .iterate(
    // How many times ?
    iterations,
    // What will be repeated ?
    chain
      // First request to be repeated
      .exec(
          http("Page accueil").get(baseUrl)
            .check(
                xpathExists(interpolate("//input[@value='{}']/@id", "aaaa_value")) in "ctxParam",
                xpathNotExists(interpolate("//input[@id='{}']/@value", "aaaa_value")) in "ctxParam2",
                regexpExists("""<input id="text1" type="text" value="aaaa" />"""),
                regexpNotExists("""<input id="text1" type="test" value="aaaa" />"""),
                statusInRange(200 to 210) in "blablaParam",
                xpathNotEquals("//input[@value='aaaa']/@id", "omg"),
                xpathEquals("//input[@id='text1']/@value", "aaaa") in "test2"
            )
      )
      .pause(pause2)
      .startGroup(loginGroup)
      .doIf("test2", "aaaa", 
          chain.exec(http("IF=TRUE Request").get(baseUrl))
          , chain.exec(http("IF=FALSE AAAA Request").get(baseUrl))
          )
      .pause(pause2)
      .exec(http("Url from context").get("http://localhost:3000/{}", "test2"))
      .pause(1000, 3000, TimeUnit.MILLISECONDS)
      // Second request to be repeated
      .exec(http("Create Thing blabla").post("http://localhost:3000/things").queryParam("login").queryParam("password").withTemplateBody("create_thing", Map("name" -> "blabla")).asJSON)
      .pause(pause1)
      .endGroup(loginGroup)
      // Third request to be repeated
      .exec(http("Liste Articles") get("http://localhost:3000/things") queryParam "firstname" queryParam "lastname")
      .pause(pause1)
      .exec(http("Test Page") get("http://localhost:3000/tests") check(headerEquals(CONTENT_TYPE, "text/html; charset=utf-8") in "ctxParam"))
      // Fourth request to be repeated
      .exec(http("Create Thing omgomg")
              .post("http://localhost:3000/things").queryParam("postTest", FromContext("ctxParam")).withTemplateBody("create_thing", Map("name" -> FromContext("ctxParam"))).asJSON
              .check(status(201) in "status"))
  )
  // Second request outside iteration
  .startGroup(doStuffGroup)
  .exec(http("Ajout au panier") get(baseUrl) capture(regexp("""<input id="text1" type="text" value="(.*)" />""") in "input"))
  .pause(pause1)
  .endGroup(doStuffGroup)