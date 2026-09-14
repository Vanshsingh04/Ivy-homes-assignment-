# Elyse Residence – Luxury Real Estate Web App

A premium, cross-platform real estate web application built using **Kotlin Multiplatform (KMP)** and **Compose for Web (Wasm)**. The application features a high-end luxury aesthetic, dynamic dark/light mode switching, and a responsive interface tailored for showcasing premium properties in Bangalore.

## ✨ Features

*   **Luxury Aesthetic & UX:** A refined, sophisticated UI with sleek "pill" filters, subtle elevations, and elegant typography inspired by premium real estate standards.
*   **Dynamic Theme Switching:** Seamlessly toggle between a warm Alabaster Light Mode and a deep Charcoal Dark Mode, fully driven by reactive state.
*   **Seamless Navigation:** A premium top-bar navigation system providing instant access to Buy, Rent, Projects, Saved, and Insights sections.
*   **Authentication State:** A mocked but functional session manager that displays a personalized profile chip and secure logout flow.
*   **Compose Multiplatform (Wasm):** Built entirely in Kotlin for the web, utilizing the latest WebAssembly (Wasm) targets for high performance and native-feeling interactions.
*   **Responsive Property Browsing:** View detailed property cards, navigate dynamic filter parameters, and view project highlights efficiently.

## 🛠 Tech Stack

*   **Language:** Kotlin
*   **Framework:** Compose Multiplatform (Compose for Web)
*   **Target:** WebAssembly (WasmJS)
*   **Build Tool:** Gradle

## 🚀 Getting Started

### Prerequisites

*   **JDK 17** or higher
*   A modern web browser with WebAssembly support (Chrome, Firefox, Safari, Edge)

### Running the Development Server

To run the application locally in development mode with hot-reloading:

```bash
./gradlew :webapp:wasmJsBrowserDevelopmentRun
```

The application will be accessible at `http://localhost:8080`.

### Building for Production

To create an optimized production build:

```bash
./gradlew :webapp:wasmJsBrowserDistribution
```

The production-ready output will be located in the `webapp/build/dist/wasmJs/productionExecutable` directory. You can serve this directory using any standard web server (e.g., Nginx, Apache, or a simple HTTP server).

## 📂 Project Structure

```
IvyHomesApp/
├── webapp/                 # The main Wasm application module
│   ├── src/
│   │   └── wasmJsMain/
│   │       ├── kotlin/
│   │       │   ├── data/       # Repositories and local token storage
│   │       │   ├── navigation/ # Custom composable NavController implementation
│   │       │   ├── theme/      # Dynamic Light/Dark mode state and palettes
│   │       │   ├── ui/         # Screens (Listings, Projects, Insights, etc.)
│   │       │   │   └── components/ # Reusable UI components (Cards, Headers, Filters)
│   │       │   └── Main.kt     # Application entry point
│   └── build.gradle.kts
└── settings.gradle.kts
```

## 🎨 Design Philosophy

The UI has been meticulously designed to prioritize content and elegance:
- **Colors:** Transitioned from high-contrast neon to a muted, elegant bronze and alabaster palette.
- **Iconography:** Utilizes robust standard Material Icons for flawless rendering across all Wasm environments.
- **Layout:** Replaced bottom-heavy navigation and dashboard-style form inputs with breathable, top-aligned desktop navigation and sleek filter pills.

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
