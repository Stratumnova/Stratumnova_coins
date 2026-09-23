# Intake and Export Contract v0.1

## Intake
Accept one file or ZIP through Android's document picker. Large sets should use ZIP.
The scanner records original name, bytes, SHA-256, media/type hints and container relationships.
Original imported bytes are immutable.

Existing Coin -> preserve master identity.
Unknown file -> candidate Coin template; unsupported fields remain UNRESOLVED.
Sideways additions are explicitly typed: version, adapter, config, asset, documentation, evidence, report, compatibility, dependency, other.

## Assets
Images: preview, fit/1x/2x/4x/8x/16x, pixel-safe scaling, frame/grid support.
Audio: play/pause/seek/loop/volume.
Assignments are stored in project metadata, never by mutating the source asset.

## Export
- Single selected file
- Complete Coin container
- Project ZIP
- Test evidence ZIP
- Reusable template ZIP

Export is a snapshot and never destroys the active workspace.
