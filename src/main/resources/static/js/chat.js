
    document.addEventListener("DOMContentLoaded", function () {
      const csrfToken = document.querySelector('meta[name="_csrf"]').content;
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

      async function sendMessage() {
          const userInput = document.getElementById("userInput").value;

          try {
              const response = await fetch("/chatbot/ask", {
                  method: "POST",
                  headers: {
                      "Content-Type": "application/json",
                      [csrfHeader]: csrfToken // CSRF 토큰 포함
                  },
                  body: JSON.stringify(userInput),
              });

              if (!response.ok) {
                  throw new Error(`Error: ${response.status} - ${response.statusText}`);
              }

              const data = await response.text();
              const chatbox = document.getElementById("chatbox");

              chatbox.innerHTML += `<p class="user"><strong>사용자:</strong> ${userInput}</p>`;
              chatbox.innerHTML += `<p class="bot"><strong>챗봇:</strong> ${data}</p>`;
              document.getElementById("userInput").value = "";
          } catch (error) {
              const chatbox = document.getElementById("chatbox");
              chatbox.innerHTML += `<p class="bot"><strong>에러:</strong> ${error.message}</p>`;
          }
      }

      document.getElementById("userInput").addEventListener("keypress", function (e) {
          if (e.key === "Enter") {
              sendMessage();
          }
      });

      document.getElementById("sendButton").addEventListener("click", sendMessage);
  });

