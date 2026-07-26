# Re-encode all MP4 files in src\main\resources\videos to JavaFX-compatible H.264/AAC
# Creates fixed copies under src\main\resources\videos\fixed
# Requirements: ffmpeg in PATH

$root = Split-Path -Parent $MyInvocation.MyCommand.Definition
$videoDir = Join-Path $root "src\main\resources\videos"
$outDir = Join-Path $videoDir "fixed"
if (-not (Test-Path $videoDir)) { Write-Error "Video folder not found: $videoDir"; exit 1 }
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Path $outDir | Out-Null }

Write-Host "Re-encoding mp4 files from: $videoDir" -ForegroundColor Cyan

Get-ChildItem -Path $videoDir -Filter *.mp4 | Where-Object { -not $_.PSIsContainer } | ForEach-Object {
    $in = $_.FullName
    $out = Join-Path $outDir ($_.BaseName + "-fixed.mp4")
    Write-Host "Processing $($_.Name) -> $([IO.Path]::GetFileName($out))"
    & ffmpeg -y -hide_banner -loglevel error -i "$in" `
        -c:v libx264 -profile:v main -level 4.0 -preset medium -crf 20 -pix_fmt yuv420p `
        -vf "scale=trunc(iw/2)*2:trunc(ih/2)*2" `
        -c:a aac -b:a 128k -movflags +faststart "$out"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ffmpeg failed for $($_.Name)" -ForegroundColor Red
    } else {
        Write-Host "OK: $($_.Name) -> $([IO.Path]::GetFileName($out))" -ForegroundColor Green
    }
}

Write-Host "Done. Fixed files are in: $outDir" -ForegroundColor Cyan
