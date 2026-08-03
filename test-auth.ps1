$body = @{
    name = "Test User"
    phone = "9999999999"
    password = "Password123!"
    role = "BUYER"
} | ConvertTo-Json

try {
    $r1 = Invoke-WebRequest -Uri http://localhost:8090/api/auth/register -Method POST -Body $body -ContentType "application/json" -UseBasicParsing
    "REGISTER STATUS: $($r1.StatusCode)`nBODY: $($r1.Content)"
} catch {
    "REGISTER STATUS: $($_.Exception.Response.StatusCode.value__)`nBODY: $(([System.IO.StreamReader]$_.Exception.Response.GetResponseStream()).ReadToEnd())"
}

$loginBody = @{
    phone = "9999999999"
    password = "Password123!"
} | ConvertTo-Json

try {
    $r2 = Invoke-WebRequest -Uri http://localhost:8090/api/auth/login -Method POST -Body $loginBody -ContentType "application/json" -UseBasicParsing
    "LOGIN STATUS: $($r2.StatusCode)`nBODY: $($r2.Content)"
} catch {
    "LOGIN STATUS: $($_.Exception.Response.StatusCode.value__)`nBODY: $(([System.IO.StreamReader]$_.Exception.Response.GetResponseStream()).ReadToEnd())"
}
