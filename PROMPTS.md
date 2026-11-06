\# AI Development Log — LedgerLine



This document records actual AI-assisted development, mistakes identified, corrections made, and related commits.



\## Entry 1 — 2026-11-06: Environment Setup



\### What I asked

Help configure a Windows development environment for a Java 21, Spring Boot, React, and PostgreSQL project.



\### AI output

Provided instructions for Java installation, JAVA\_HOME configuration, and Maven verification.



\### What was wrong

The existing JAVA\_HOME pointed to C:\\Java\\jdk-26, which was not a valid installation.



\### What I did instead

Installed Eclipse Temurin JDK 21.0.13, corrected JAVA\_HOME, opened a new CMD window, and verified Java, Javac, and Maven.



\### Verification

\- Java 21.0.13: Passed

\- Javac 21.0.13: Passed

\- Maven 3.10.0: Passed



\### Related commit

https://github.com/komalikotte-ai/ledgerline/commit/aa8caa1



\---



\## Entry 2 — 2026-11-06: Spring Boot Backend Foundation



\### What I asked

Help generate a Spring Boot backend with Java 21, Maven, Spring Security, Spring Data JPA, PostgreSQL, Flyway, validation, and Actuator.



\### AI output

Provided a Spring Initializr download command and instructions for extracting and testing the backend.



\### What was wrong

The initially requested Spring Boot 3.5.7 version was rejected by Spring Initializr because its compatibility range required version 4.0.0 or newer.



The first application context test also failed because no datasource configuration was available.



\### What I did instead

Changed the generated project to Spring Boot 4.0.0, added H2 as a test-only dependency, configured an in-memory test database, disabled Flyway for the test profile, and activated that profile in the test class.



I also resolved a Windows file-locking issue that prevented Maven from cleaning its target directory.



\### Verification

\- Backend extraction: Passed

\- Java compilation: Passed

\- Application context test: Passed

\- Tests run: 1

\- Failures: 0

\- Errors: 0

\- Maven build: BUILD SUCCESS



\### Related commit

Pending backend foundation commit.

