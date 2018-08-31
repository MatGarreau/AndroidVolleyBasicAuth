# Android Volley Basic Authent

## Description

this little Android application has been written to consume the API Rest develop in Go and available here :

[Go API Rest](https://github.com/MatGarreau/GoApiRestBasicAuth)

I use Volley library to send simple HTTP request, and HTTP request that need authentication with Basic Authent.

The screen should look like :

![](screenshot.png)

## Without authentication

The first button allows to send a request to the raspberry pi and get the API status (UP or DOWN)

## With authentication

The second button allows to get the GPIO status (GPIO 17) to know the level is High (led is ON) or Low (led is OFF)

The third button allows to switch ON the led on GPIO 17

The fourth button allows to switch OFF the led on GPIO 17

