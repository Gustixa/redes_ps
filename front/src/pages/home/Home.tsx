import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useAuth } from '@contexts/AuthContext';

const Home: React.FC = () => {
  const { authUser } = useAuth();
  const [contacts, setContacts] = useState<string[]>([]);

  useEffect(() => {
    const fetchContacts = async () => {
      if (!authUser) return;

      console.log('Fetching contacts for user:', authUser);

      try {
        const response = await axios.get(`http://localhost:8000/contacts`, {
          params: { username: authUser.correo }
        });
        setContacts(response.data);
      } catch (error) {
        console.error('Error fetching contacts:', error);
      }
    };

    fetchContacts();
  }, [authUser]);

  return (
    <div>
      <h1>Contacts</h1>
      {contacts.length > 0 ? (
        <div>
          <h2>Contacts List</h2>
          <ul>
            {contacts.map((contact, index) => (
              <li key={index}>{contact}</li>
            ))}
          </ul>
        </div>
      ) : (
        <p>Loading contacts...</p>
      )}
    </div>
  );
};

export default Home;
