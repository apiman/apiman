import { defineConfig } from 'cypress'

export default defineConfig({
  projectId: 'tpeh3n',
  defaultCommandTimeout: 10000,
  chromeWebSecurity: false,
  e2e: {
    // We've imported your old cypress plugins here.
    // You may want to clean this up later by importing these.
    setupNodeEvents(on, config) {
      return require('./cypress/plugins/index.js')(on, config)
    },
    baseUrl: 'http://localhost:2772/apimanui/',
    specPattern: 'cypress/e2e/**/*.{js,jsx,ts,tsx}',
  },
})
