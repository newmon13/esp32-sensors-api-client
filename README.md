# ESP32 Sensors API Client

![Java](https://img.shields.io/badge/Java-19-brightgreen)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green)

Application provides a dashboard to display data from various analog/digital sensors connected to single(multiple in future) ESP32 microcontroller. 

## Table of Contents

1. [About](#about)
2. [Features](#features)
3. [Technologies](#technologies)
4. [Getting Started](#getting-started)

## About

This service provides functionality to process and manage SWIFT/BIC codes data. It extracts bank information from spreadsheets, stores it in a database, and makes it accessible through RESTful endpoints. The service handles both headquarters and branch offices, with special processing for their relationships.

## Features

Core functionality:
- ✅ Retreving data from sensors
- ✅ Visual representation of received data
- [] Mail notification if data from sensor reached critical level
- [] Asynchronous communication between spring module and sensor APIs

Nice to have:
- [] Edge cases validation
- [] Comprehensive test coverage (unit & integration)
- [] Containerized deployment
- [] Scalability (using event queuing system)

## Technologies

- Java 19
- Spring Boot
- Python
- Docker
- Mosquitto (message broker that implements MQTT protocol dedicated for IoT)
- JUnit & Mockito & Testcontainers for testing

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Java 19 (for local development)

### Running with Docker




