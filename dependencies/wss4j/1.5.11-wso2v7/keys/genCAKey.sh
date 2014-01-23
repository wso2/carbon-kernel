#!/bin/sh

openssl req -x509 -newkey rsa:1024 -keyout CAKey.pem -out CA.pem -config ca.config

