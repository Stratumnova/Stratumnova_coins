# Build by Coin — Premade Market Test Room v0.1

Status: implementation target

## Purpose
A deliberately plain, deterministic 25x25 tile room used to prove that loaded Coins, runtime adapters, assets, crafting, stalls, NPCs, customers, currency, placement, pathways, evidence, and export work together in the same runtime.

The room is test equipment, not final game art. Generic geometry and labels are authoritative fallbacks. Imported ZIP assets may replace visuals without replacing the logical object identity.

## World grid
- Width: 25 tiles
- Height: 25 tiles
- Coordinates: X=00..24, Y=00..24
- Every tile has a permanent space code: `S-X##-Y##`.
- Example: `S-X07-Y14`.
- Tile state records: space_code, x, y, terrain, walkable, occupied_by, reserved_for, visual_asset.
- Pathfinding and placement use space codes, not screen pixels.
- Outer edge is blocked except explicitly declared entrances.
- Runtime state/evidence records object placement by space code.

## Test economy
One currency: `CREDIT`.

Raw parts:
- PART-A — blue square
- PART-B — yellow triangle
- PART-C — red circle
- PART-D — green diamond
- PART-E — purple hexagon
- PART-F — orange bar

Crafted items:
- ITEM-1 = PART-A + PART-B — cyan box — sale value 10 CREDIT
- ITEM-2 = PART-C + PART-D — magenta disk — sale value 12 CREDIT
- ITEM-3 = PART-E + PART-F — amber capsule — sale value 15 CREDIT

Each craft consumes exactly one of each required part and creates one item.

## Stalls and NPCs
Three stalls:
- STALL-1 crafts/sells ITEM-1
- STALL-2 crafts/sells ITEM-2
- STALL-3 crafts/sells ITEM-3

Three stall NPCs:
- VENDOR-1 assigned STALL-1
- VENDOR-2 assigned STALL-2
- VENDOR-3 assigned STALL-3

Three customer NPCs:
- CUSTOMER-1
- CUSTOMER-2
- CUSTOMER-3

Customer loop:
ENTER -> choose stall -> walk by space-code path -> queue -> buy if stock and currency permit -> leave.

Vendor loop:
CHECK PARTS -> CRAFT when recipe is available -> STOCK STALL -> WAIT/SELL -> repeat.

## Default room placement
Keep the center and lower half open so routes are visually obvious.

- STALL-1 anchor: S-X05-Y05
- STALL-2 anchor: S-X12-Y05
- STALL-3 anchor: S-X19-Y05
- VENDOR-1: S-X05-Y07
- VENDOR-2: S-X12-Y07
- VENDOR-3: S-X19-Y07
- CUSTOMER entrance: S-X12-Y23
- Customer spawn/queue positions use nearby unoccupied walkable spaces.
- Parts bins sit behind/adjacent to their matching stall.
- Main aisle runs from entrance toward Y08 with branches to all three stalls.

Exact footprints must be declared in room data; anchors do not imply pixel positions.

## Generic visual IDs
Fallback rendering is intentionally simple:
- floor: gray tile carrying short space-code overlay when debug grid is enabled
- blocked tile: dark square with X
- stall: outlined rectangle labeled S1/S2/S3
- vendor: square labeled V1/V2/V3
- customer: circle labeled C1/C2/C3
- parts: colored primitive + A/B/C/D/E/F
- items: colored primitive + I1/I2/I3
- pathway: small center dot/line
- selected object: cyan outline

A debug toggle displays every tile's `S-X##-Y##` code.

## Asset substitution
The logical visual ID and the imported visual asset are separate fields.

Example:
```json
{
  "entity_id": "VENDOR-1",
  "visual_id": "vendor-1",
  "asset_ref": "UNRESOLVED"
}
```

When a ZIP supplies a compatible image/audio asset, the user can map it to a visual ID. The runtime continues to use the same entity ID, placement, collision, recipe, and behavior. Removing the asset falls back to the generic shape.

No imported asset silently changes mechanics.

## Runtime events
Minimum event vocabulary:
- ENTITY_SPAWNED
- PATH_REQUESTED
- PATH_STEP
- PATH_BLOCKED
- PART_ADDED
- PART_CONSUMED
- CRAFT_STARTED
- CRAFT_COMPLETED
- ITEM_STOCKED
- CUSTOMER_QUEUED
- SALE_COMPLETED
- SALE_REJECTED
- CREDIT_CHANGED

Events involving a world location include its space_code.

## Test controls
PLAY: user may select/move a test actor and trigger craft/stock/sale actions.
AUTO: seeded driver runs customer/vendor/economy loop.
WATCH: same AUTO commands while displaying the actual room.
RESET: restores declared initial state and seed.
SAVE EVIDENCE: writes assembly identity, seed, inputs, events, placements and before/after state hashes.

## Default initial state
- Seed: 25
- Currency account: MARKET = 0 CREDIT
- Each customer: 30 CREDIT
- Each stall: 0 crafted stock
- Each recipe bin: 3 of each required part
- All six NPCs spawned.
- No asset pack required.

This guarantees at least three crafts and potential sales without external files.

## Functional UI direction
The APK should feel like a compact Linux/AI workbench rather than a decorative mobile game editor:
- black/dark background
- high-contrast text
- cyan for active/selected state
- green for successful runtime/evidence output
- compact rectangular controls
- monospace where data is being inspected
- visible state and explicit commands instead of hidden gestures
- plain lists/tables before cards
- modal dialogs only when a decision cannot fit inline

Persistent primary screens:
`TERMINAL | FILES | BUILD | RUN | EXPORT`

Assets open from FILES. Code/text editor opens from FILES or BUILD. The RUN screen owns this premade room and can expand it fullscreen.

## Acceptance
The room passes when the APK can:
1. initialize all 625 unique space codes;
2. render the generic room without external assets;
3. place all stalls/NPCs by space code;
4. route customers without using screen coordinates as authority;
5. craft all three recipes;
6. stock and sell all three items using CREDIT;
7. replace generic visuals with mapped compatible ZIP assets while preserving mechanics;
8. run PLAY/AUTO/WATCH through the same RuntimeCore;
9. save deterministic event/state evidence;
10. export a portable project that reproduces the same logical room.
