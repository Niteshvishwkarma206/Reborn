const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_hunter_123";
const BOB_UID = "bob_hunter_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Unauthenticated user: cannot read user missions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("missions").get());
});

test("Authenticated user: cannot read another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      name: "Bob Hunter",
      level: 2,
      currentXp: 100,
      currentChallengeDay: 1,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: cannot read another user's missions", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("missions").doc("m1_1").set({
      id: "m1_1",
      day: 1,
      title: "Awakening Walk",
      category: "Walk",
      xpReward: 50,
      isCompleted: true,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("missions").doc("m1_1").get());
});

test("Authenticated user: can create and read their own user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(profileRef.set({
    userId: ALICE_UID,
    name: "Alice Hunter",
    level: 1,
    currentXp: 50,
    currentChallengeDay: 1,
  }));

  await assertSucceeds(profileRef.get());
});

test("Authenticated user: can record and list their own walking strides", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const stridesRef = aliceDb.collection("users").doc(ALICE_UID).collection("strides");

  await assertSucceeds(stridesRef.doc("stride_1").set({
    id: "stride_1",
    activityType: "Walking",
    distanceKm: 4.5,
    durationMinutes: 45,
    steps: 5400,
    date: "Today",
    notes: "Morning jog",
  }));

  await assertSucceeds(stridesRef.get());
});
