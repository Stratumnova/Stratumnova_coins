# Runtime Contract v0.1

1. Lab and exported game execute the same authoritative runtime mechanisms.
2. Terminal, telemetry and test drivers are observers/command sources, not replacement mechanics.
3. Tests record runtime version, exact artifact hashes, seed, tick rate, initial state and inputs.
4. Deterministic state excludes wall-clock timestamps, device identity, render timing and filesystem paths.
5. PLAY, AUTO and WATCH differ only by command source/presentation.
6. A standalone export must be capable of replaying the same deterministic scenario and matching authoritative state hashes.
7. Unsupported artifacts fail as UNRESOLVED/UNSUPPORTED; they never receive fabricated PASS results.
