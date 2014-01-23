SET PATH=C:\Program Files\Microsoft Visual Studio .NET 2003\SDK\v1.1\Bin;%PATH%
DEL /F selfcert.*
makecert -sky exchange -r -n "CN=Davanum Srinivas" -b 01/01/2000 -e 01/01/2099 -sv selfcert.pvk selfcert.cer
cert2spc selfcert.cer selfcert.spc
pvkimprt -PFX selfcert.spc selfcert.pvk