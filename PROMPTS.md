\# AI Development Log — LedgerLine



This document records actual AI-assisted development,

mistakes identified, corrections and related commits.



\## Entry 1 — 2026-11-06



\### Task

Prepare the development environment for LedgerLine.



\### Prompt

Help configure a Windows development environment

for a Java 21, Spring Boot, React and PostgreSQL project.



\### AI assistance

Provided Java installation and environment configuration

instructions.



\### Issue identified

The existing JAVA\_HOME variable pointed to C:\\Java\\jdk-26,

while the available Java installation was JDK 17.



Maven could not start because JAVA\_HOME was invalid.



\### My correction

Installed Eclipse Temurin JDK 21.0.13.

Updated JAVA\_HOME to the actual JDK 21 installation directory.

Opened a new CMD window and verified Java, Javac and Maven.



\### Verification

\- Java 21.0.13: Verified

\- Javac 21.0.13: Verified

\- Maven 3.10.0: Verified



\### Related commit

Pending initial environment documentation commit.

