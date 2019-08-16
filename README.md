1. Run command example: 

-b "{\"at\":2,\"cur\":[\"GBP\"],\"device\":{\"connectiontype\":0,\"devicetype\":2,\"dnt\":0,\"geo\":{\"country\":\"GBR\"},\"lmt\":0,\"ua\":\"Videology/private\"},\"ext\":{\"wcrid\":[\"1606209777\"],\"wtrack\":[\"*.turn.com\"]},\"id\":\"7691540270125224228\",\"imp\":[{\"bidfloor\":0,\"id\":\"f940ec0b-eaab-4e58-b44e-cfd2338a26ce\",\"pmp\":{\"deals\":[{\"at\":501,\"bidfloor\":5,\"bidfloorcur\":\"GBP\",\"id\":\"648579\"}],\"private_auction\":1},\"secure\":0,\"video\":{\"linearity\":1,\"maxduration\":60,\"mimes\":[\"video/x-ms-wmv\",\"application/x-mpegurl\",\"video/x-flv\",\"video/webm\",\"video/mp2t\",\"video/mpeg\",\"application/vnd.ms-sstr\",\"application/f4m\",\"text/xml\",\"video/ogg\",\"video/mp4\",\"application/x-ttv-universalurl\"],\"minduration\":0,\"playbackmethod\":[3],\"pos\":0,\"protocols\":[2,3,5]}}],\"regs\":{\"coppa\":0,\"ext\":{\"gdpr\":1}},\"site\":{\"domain\":\"itv.com\",\"page\":\"http://itv.com\"}}" -url http://localhost:8000/itv.bid -m POST -t 5 -rps 100 


Parameter to use:
u/url : request URL
m/method : http method
b/body : http body
t/time : runtime
r/rps : request per second

t*r is the total request to run

2. Command to build jar file
gradle fatJar 
New jar is built here .//build/libs/http-benchmark-tool-1.0.jar
