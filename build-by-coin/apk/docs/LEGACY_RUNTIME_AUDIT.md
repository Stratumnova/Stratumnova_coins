# Legacy Runtime Audit

Status: dependency classification after live 9x9 World replacement.

The live game authority is the World chain:
WorldStore -> WorldEditor / WorldLayoutView -> WorldSession -> WorldRunView.

The former 25x25 TestRoom is not the live World and must not regain authority.

## KEEP / EXTRACT AS REUSABLE MECHANICS
- runtime/GameClock.kt — reusable clock.
- runtime/SeededRng.kt — deterministic RNG.
- runtime/WorldEnvironment.kt — reusable environment/day/weather concepts; adapt to World.
- runtime/MarketTestState.kt — specialized market/crafting mechanic; expose through Tools or port into World entities.
- runtime/RuntimeAdapter.kt and runtime/ActiveAssembly.kt — keep while Coin/runtime assembly path is still used.
- test/TestHarness.kt and test/EvidenceWriter.kt — keep for diagnostics/evidence, but retarget tests to live World assemblies.

## SPECIALIZED TOOL, NOT LIVE WORLD AUTHORITY
- test/TestPanel.kt — old laboratory UI. Do not route primary Run through it.
- runtime/RuntimeCore.kt — old fixture runtime; mine reusable mechanics only.
- runtime/GameView.kt — old renderer; live rendering is WorldRunView.
- runtime/FixtureProfile.kt — old fixture profiles; retain only as reference/tool data while mechanics migrate.
- runtime/MarketTestState.kt — Market Station specialization.
- docs/PREMADE_MARKET_TEST_ROOM_v0.1.md — historical fixture documentation.

## OBSOLETE AFTER DEPENDENCY EXTRACTION
- runtime/TestRoom.kt — 25x25 substrate replaced by movable 9x9 Cells.
- old 25x25-specific rendering/placement assumptions inside RuntimeCore, GameView, TestPanel and FixtureProfile.

## REMOVAL RULE
Do not delete a legacy file merely because the live World replaced it. First prove no active intake, Coin, evidence, export, or reusable mechanic depends on it. Extract useful mechanics into neutral modules, redirect callers, obtain a green build, then delete the obsolete file in a separate cleanup checkpoint.

## RED/BUILD RULE
A failed Actions run stops feature work. Inspect its compiler/test log, repair the failure, obtain a green branch-head build, then continue.
