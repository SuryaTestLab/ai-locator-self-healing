# Selenium Locator Helper

Note: Goal is to generate locators using AI (in progress)

A Java project for extracting and managing element signatures from web pages using Selenium WebDriver.

## Features

- Extracts element attributes, CSS/XPath selectors, and neighbor text.
- Uses Selenium WebDriver for browser automation.
- JSON serialization support via Jackson.

## Requirements

- Java 17+
- Maven 3.6+
- Selenium WebDriver
- Jackson

## Setup

1. Clone the repository.
2. Run `mvn clean install` to build the project.
3. Add your Selenium WebDriver binaries to the system path.

## Usage

Example usage in your test code:

```java
WebDriver driver = new ChromeDriver();
WebElement element = driver.findElement(By.id("username"));
ElementSignature signature = ElementSignature.from(driver, element, "login.username.input");
System.out.println(signature);
