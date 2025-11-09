-- Insert sample users for development and testing
INSERT INTO users (username, email, password, full_name, bio, is_active, created_at, updated_at) VALUES
('admin', 'admin@snipper.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Administrator', 'System administrator account', TRUE, NOW(), NOW()),
('johndoe', 'john.doe@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John Doe', 'Full-stack developer passionate about clean code', TRUE, NOW(), NOW()),
('janedoe', 'jane.doe@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Jane Doe', 'Frontend developer and UI/UX enthusiast', TRUE, NOW(), NOW());

-- Insert sample snippets for development and testing
INSERT INTO snippets (title, description, content, language, tags, visibility, view_count, author_id, created_at, updated_at) VALUES
('Hello World in Java', 'A simple Hello World program in Java', 'public class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println("Hello, World!");\n    }\n}', 'java', 'java,hello-world,beginner', 'PUBLIC', 15, 2, NOW(), NOW()),

('React Functional Component', 'A basic React functional component with hooks', 'import React, { useState, useEffect } from ''react'';\n\nconst MyComponent = () => {\n    const [count, setCount] = useState(0);\n\n    useEffect(() => {\n        document.title = `Count: ${count}`;\n    }, [count]);\n\n    return (\n        <div>\n            <h1>Count: {count}</h1>\n            <button onClick={() => setCount(count + 1)}>\n                Increment\n            </button>\n        </div>\n    );\n};\n\nexport default MyComponent;', 'javascript', 'react,javascript,hooks,frontend', 'PUBLIC', 32, 3, NOW(), NOW()),

('Python List Comprehension', 'Examples of Python list comprehensions', '# Basic list comprehension\nnumbers = [1, 2, 3, 4, 5]\nsquares = [x**2 for x in numbers]\nprint(squares)  # [1, 4, 9, 16, 25]\n\n# List comprehension with condition\neven_squares = [x**2 for x in numbers if x % 2 == 0]\nprint(even_squares)  # [4, 16]\n\n# Nested list comprehension\nmatrix = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]\nflattened = [item for row in matrix for item in row]\nprint(flattened)  # [1, 2, 3, 4, 5, 6, 7, 8, 9]', 'python', 'python,list-comprehension,functional-programming', 'PUBLIC', 28, 2, NOW(), NOW()),

('CSS Flexbox Layout', 'A responsive flexbox layout example', '.container {\n    display: flex;\n    flex-direction: row;\n    justify-content: space-between;\n    align-items: center;\n    flex-wrap: wrap;\n    gap: 1rem;\n    padding: 1rem;\n}\n\n.item {\n    flex: 1 1 300px;\n    min-height: 200px;\n    background: #f0f0f0;\n    border-radius: 8px;\n    padding: 1rem;\n}\n\n@media (max-width: 768px) {\n    .container {\n        flex-direction: column;\n    }\n    \n    .item {\n        flex: 1 1 auto;\n    }\n}', 'css', 'css,flexbox,responsive,layout', 'PUBLIC', 19, 3, NOW(), NOW()),

('Private Configuration', 'My private server configuration', 'server {\n    listen 80;\n    server_name example.com;\n    \n    location / {\n        proxy_pass http://localhost:3000;\n        proxy_set_header Host $host;\n        proxy_set_header X-Real-IP $remote_addr;\n    }\n}', 'nginx', 'nginx,configuration,server', 'PRIVATE', 2, 2, NOW(), NOW()),

('SQL Query Optimization', 'Optimized SQL query for user analytics', 'SELECT \n    u.username,\n    COUNT(s.id) as snippet_count,\n    AVG(s.view_count) as avg_views,\n    MAX(s.created_at) as last_snippet_date\nFROM users u\nLEFT JOIN snippets s ON u.id = s.author_id\nWHERE u.is_active = TRUE\n    AND s.visibility = ''PUBLIC''\nGROUP BY u.id, u.username\nHAVING snippet_count > 0\nORDER BY avg_views DESC, snippet_count DESC\nLIMIT 10;', 'sql', 'sql,analytics,optimization,mysql', 'UNLISTED', 7, 2, NOW(), NOW());

-- Update view counts to simulate real usage
UPDATE snippets SET view_count = view_count + FLOOR(RAND() * 50) WHERE visibility = 'PUBLIC';
