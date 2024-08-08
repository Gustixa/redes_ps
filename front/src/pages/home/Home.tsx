import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useAuth } from '@contexts/AuthContext';


interface Message {
  from: string;
  body: string;
}

interface Conversations {
  [username: string]: Message[];
}

const Home: React.FC = () => {
  const { authUser } = useAuth();
  const [conversations, setConversations] = useState<Conversations>({});

  useEffect(() => {
    const fetchConversations = async () => {
      if (!authUser) return;

      console.log('Fetching conversations for user:', authUser); // Verifica el valor de `user`

      try {
        const response = await axios.get(`http://localhost:8000/conversations/${authUser.correo}`);
        setConversations(response.data);
      } catch (error) {
        console.error('Error fetching conversations:', error);
      }
    };

    fetchConversations();
  }, [authUser]);

  return (
    <div>
      <h1>Home</h1>
      {Object.keys(conversations).length > 0 ? (
        <div>
          {Object.entries(conversations).map(([username, messages]) => (
            <div key={username}>
              <h2>{username}</h2>
              {messages.length > 0 ? (
                <ul>
                  {messages.map((message, index) => (
                    <li key={index}>
                      <strong>{message.from}:</strong> {message.body}
                    </li>
                  ))}
                </ul>
              ) : (
                <p>No messages</p>
              )}
            </div>
          ))}
        </div>
      ) : (
        <p>Loading conversations...</p>
      )}
    </div>
  );
};

export default Home;