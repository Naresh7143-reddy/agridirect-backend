$baseUrl = "https://agridirect-backend-80yz.onrender.com/api"

$endpoints = @(
    @{ name = "Get Categories"; method = "GET"; url = "$baseUrl/categories"; body = $null },
    @{ name = "Get Products"; method = "GET"; url = "$baseUrl/products"; body = $null },
    @{ name = "Delivery Availability (Missing Params)"; method = "GET"; url = "$baseUrl/delivery/availability"; body = $null },
    @{ name = "Delivery Availability (Valid)"; method = "GET"; url = "$baseUrl/delivery/availability?latitude=12.9716&longitude=77.5946"; body = $null },
    @{ name = "Invalid Endpoint"; method = "GET"; url = "$baseUrl/invalid-endpoint-test"; body = $null }
)

$results = @()

foreach ($ep in $endpoints) {
    try {
        if ($ep.method -eq "GET") {
            $r = Invoke-WebRequest -Uri $ep.url -Method GET -UseBasicParsing
        } else {
            $r = Invoke-WebRequest -Uri $ep.url -Method POST -Body $ep.body -ContentType "application/json" -UseBasicParsing
        }
        $results += @{ Name = $ep.name; URL = $ep.url; Status = $r.StatusCode; Error = $null }
    } catch {
        $status = if ($_.Exception.Response) { $_.Exception.Response.StatusCode.value__ } else { "ERROR" }
        $results += @{ Name = $ep.name; URL = $ep.url; Status = $status; Error = $_.Exception.Message }
    }
}

$results | ConvertTo-Json -Depth 2
