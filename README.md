# Android Volley Basic Authent

## Description

this little Android application has been written to consume the API Rest develop in Go and available here :

[Go API Rest](https://github.com/MatGarreau/GoApiRestBasicAuth)

I use Volley library to send simple HTTP request, and HTTP request that need authentication with Basic Authent.

The screen should look like :

![](screenshot2.png)

## Without authentication

The first button allows to send a request to the raspberry pi and get the API status (UP or DOWN)

## With authentication

The second button allows following actions :

* get the GPIO status (GPIO 17) to know the level is High (led is ON) or Low (led is OFF)

* switch ON the led

* switch OFF the led

# v0.21

* manage many GPIOs



