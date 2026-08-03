const { Client } = require('pg');

const client = new Client({
  host: 'aws-1-ap-southeast-2.pooler.supabase.com',
  port: 5432,
  database: 'postgres',
  user: 'postgres.otgnhaevwkzxmvjekbmn',
  password: 'Naresh@7143.',
  ssl: { rejectUnauthorized: false }
});

console.log('Attempting to connect to database on 5432...');
client.connect(err => {
  if (err) {
    console.error('Connection failed:', err.message);
  } else {
    console.log('Connection successful!');
    client.end();
  }
});
