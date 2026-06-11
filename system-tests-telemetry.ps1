$baseUrl = "http://localhost:8080"
$token = "eyJhbGciOiJFUzI1NiIsImtpZCI6IjM3NTlkZGUxLWI5YjctNGE4OC1hMzEzLWVmNTgyYjRiMDNiNCIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2NpZXFmaHdlcnB4b21mdmVsb2pxLnN1cGFiYXNlLmNvL2F1dGgvdjEiLCJzdWIiOiJmZTNjZDVhMC0yMmIyLTQ0ZGEtYmNmZS05NzIzOTM4YTA5YjIiLCJhdWQiOiJhdXRoZW50aWNhdGVkIiwiZXhwIjoxNzgxMjA0NTQ0LCJpYXQiOjE3ODEyMDA5NDQsImVtYWlsIjoibWlhc3JhZmFAZ21haWwuY29tIiwicGhvbmUiOiIiLCJhcHBfbWV0YWRhdGEiOnsicHJvdmlkZXIiOiJlbWFpbCIsInByb3ZpZGVycyI6WyJlbWFpbCJdfSwidXNlcl9tZXRhZGF0YSI6eyJlbWFpbCI6Im1pYXNyYWZhQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoiUmFmYWVsIFNhbnRvcyIsInBob25lIjoiMTE5NTk2MzI4OTgiLCJwaG9uZV92ZXJpZmllZCI6ZmFsc2UsInN1YiI6ImZlM2NkNWEwLTIyYjItNDRkYS1iY2ZlLTk3MjM5MzhhMDliMiJ9LCJyb2xlIjoiYXV0aGVudGljYXRlZCIsImFhbCI6ImFhbDEiLCJhbXIiOlt7Im1ldGhvZCI6InBhc3N3b3JkIiwidGltZXN0YW1wIjoxNzgxMjAwOTQ0fV0sInNlc3Npb25faWQiOiJhZTA5M2Q5ZS0yN2M0LTRhZGItYjIzZS0wZDQ4ZTQxMDA2MjEiLCJpc19hbm9ueW1vdXMiOmZhbHNlfQ.rkQj7IndYMCheC3MgqvOOkxyytwBGW0DfyH-46o2zBr8a2QG7Rt5mG_E18ZgWDsDBBtcDx1W3FENKMtIhFWdVw"
$smartPotId = "273f9192-d2c1-467d-9855-3f0e502e9f42"
$otherUserSmartPotId = "e90721e5-f8fc-442e-af4e-a96256b90a52"
$invalidSmartPotId = "00000000-0000-0000-0000-000000000000"

function Run-Test {
    param (
        [string]$Name,
        [string]$Url,
        [string]$Body,
        [int]$ExpectedStatus
    )

    Write-Host "`nExecutando $Name..." -ForegroundColor Cyan

    try {
        $response = Invoke-WebRequest `
            -Uri $Url `
            -Method POST `
            -Headers @{ Authorization = "Bearer $token" } `
            -ContentType "application/json" `
            -Body $Body

        $actualStatus = [int]$response.StatusCode
        $content = $response.Content
    }
    catch {
        $actualStatus = [int]$_.Exception.Response.StatusCode.value__
        $stream = $_.Exception.Response.GetResponseStream()

        if ($stream -ne $null) {
            $reader = New-Object System.IO.StreamReader($stream)
            $content = $reader.ReadToEnd()
        } else {
            $content = $_.Exception.Message
        }
    }

    if ($actualStatus -eq $ExpectedStatus) {
        Write-Host "$Name - OK - Status $actualStatus" -ForegroundColor Green
    } else {
        Write-Host "$Name - NOK - Esperado $ExpectedStatus, recebido $actualStatus" -ForegroundColor Red
        Write-Host "Resposta:"
        Write-Host $content
    }
}

Run-Test `
    -Name "CT01 - Leitura valida sem alerta" `
    -Url "$baseUrl/telemetry?smartPotId=$smartPotId" `
    -ExpectedStatus 201 `
    -Body '{
        "soilHumidity": 50,
        "airHumidity": 60.0,
        "temperature": 25.0,
        "luminosityStatus": "CLARO"
    }'

Run-Test `
    -Name "CT02 - Leitura valida com alerta" `
    -Url "$baseUrl/telemetry?smartPotId=$smartPotId" `
    -ExpectedStatus 201 `
    -Body '{
        "soilHumidity": 29,
        "airHumidity": 60.0,
        "temperature": 25.0,
        "luminosityStatus": "CLARO"
    }'

Run-Test `
    -Name "CT03 - Valor limite inferior aceito" `
    -Url "$baseUrl/telemetry?smartPotId=$smartPotId" `
    -ExpectedStatus 201 `
    -Body '{
        "soilHumidity": 0,
        "airHumidity": 60.0,
        "temperature": 25.0,
        "luminosityStatus": "CLARO"
    }'

Run-Test `
    -Name "CT04 - Valor limite superior aceito" `
    -Url "$baseUrl/telemetry?smartPotId=$smartPotId" `
    -ExpectedStatus 201 `
    -Body '{
        "soilHumidity": 100,
        "airHumidity": 60.0,
        "temperature": 25.0,
        "luminosityStatus": "CLARO"
    }'

Run-Test `
    -Name "CT07 - Payload incompleto" `
    -Url "$baseUrl/telemetry?smartPotId=$smartPotId" `
    -ExpectedStatus 400 `
    -Body '{
        "airHumidity": 60.0,
        "temperature": 25.0,
        "luminosityStatus": "CLARO"
    }'

Run-Test `
    -Name "CT08 - SmartPot inexistente" `
    -Url "$baseUrl/telemetry?smartPotId=$invalidSmartPotId" `
    -ExpectedStatus 404 `
    -Body '{
        "soilHumidity": 50,
        "airHumidity": 60.0,
        "temperature": 25.0,
        "luminosityStatus": "CLARO"
    }'

Run-Test `
    -Name "CT09 - SmartPot de outro usuario" `
    -Url "$baseUrl/telemetry?smartPotId=$otherUserSmartPotId" `
    -ExpectedStatus 403 `
    -Body '{
        "soilHumidity": 50,
        "airHumidity": 60.0,
        "temperature": 25.0,
        "luminosityStatus": "CLARO"
    }'

Run-Test `
    -Name "CT10 - Umidade menor que zero" `
    -Url "$baseUrl/telemetry?smartPotId=$smartPotId" `
    -ExpectedStatus 400 `
    -Body '{
        "soilHumidity": -1,
        "airHumidity": 60.0,
        "temperature": 25.0,
        "luminosityStatus": "CLARO"
    }'

Run-Test `
    -Name "CT11 - Umidade maior que cem" `
    -Url "$baseUrl/telemetry?smartPotId=$smartPotId" `
    -ExpectedStatus 400 `
    -Body '{
        "soilHumidity": 101,
        "airHumidity": 60.0,
        "temperature": 25.0,
        "luminosityStatus": "CLARO"
    }'

