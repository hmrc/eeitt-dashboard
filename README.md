How to Run
==========

go to Google dev console : https://console.developers.google.com/

go to credentials tab on the left hand side.

go to create credentials.

click OAuth Client Id

When in new page select Other, supply desired name.

Download the json file that this creates to your machine and save it in the src/main/resources with the
file name of servicedata.json

now with the desired Google sheet. collect the ID which is the random string in the url.

add this to a file called serviceAccount.json in src/main/resources again. in the format off :

{
  "fileId": ""
}
