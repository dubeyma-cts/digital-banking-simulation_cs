# ibn-ui Module

This module contains the Angular front-end application for the IBN Enterprise App. It is responsible for the user interface and user experience.

## Project Structure

- **src/**: Contains the source code for the Angular application.
  - **app/**: Contains the main application module and components.
    - **components/**: Contains reusable components such as header and footer.
    - **services/**: Contains services for making API calls to the back-end.
  - **index.html**: The main HTML file that serves as the entry point for the application.
  - **main.ts**: The main TypeScript file that bootstraps the Angular application.
  - **polyfills.ts**: Includes polyfills needed for the application to run in various browsers.
  - **styles.css**: Contains global styles for the application.

## Development

To run the application, navigate to the `ibn-ui` directory and use the following command:

```bash
npm install
ng serve
```

This will start the Angular development server, and you can access the application at `http://localhost:4200`.

## Build

To build the application for production, use the following command:

```bash
ng build --prod
```

This will create a `dist/` directory with the production-ready files.

## Dependencies

This project uses several dependencies defined in `package.json`. Make sure to check it for the list of libraries and frameworks used in the application.

## Contribution

Feel free to contribute to this project by submitting issues or pull requests. Please ensure that your code adheres to the project's coding standards and includes appropriate tests.

## License

This project is licensed under the MIT License. See the LICENSE file for more details.