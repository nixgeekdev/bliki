# bliki

A bliki is a cross between a wiki and a blog. Like a blog, it allows me to post short thoughts when I have them. 
Like a wiki it will build up a body of cross-linked pieces that I hope will still be interesting in a year's time.

## what I'm trying to do

What I want to do is a hybrid web app that merges both features of a blog and a wiki... Let's call it a bliki. 
I want you to keep the MVP approach to the design, but for context, here are some requirements:

1. publish timestamped entries in reverse chronological order at permanent URLs
2. an entry listing page
3. a single entry page
4. storage in a postgresql database
5. no comments on entries
6. multiple users - a user is also an author of entries
7. users need to login
8. users have roles for permissions such as ADMIN, AUTHOR, CONTRIBUTOR
9. each entry should have a unique url slug - permanent addressability is essential
10. content will be written in markdown format
11. entries should be easily related to each other
12. keep a history of entry edits
13. entries should be easily discoverable via a seach feature
14. entries should use tags to create a taxonomy of content
15. entries are structure both in a timeline and in a graph
